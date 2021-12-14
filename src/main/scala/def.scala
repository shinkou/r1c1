package com.shinkou.r1c1

import java.io.{FileReader, FileWriter}
import java.sql.{Connection, DriverManager, ResultSet}
import java.util.Date

import scala.collection.JavaConversions._
import scala.collection.mutable.{Map, Set}

import org.apache.commons.configuration2.PropertiesConfiguration

import com.opencsv.CSVWriter
import com.jcraft.jsch.{JSch, Session}

import org.slf4j.LoggerFactory

class QuerySet(val url :String, val user :Option[String] = None, val password :Option[String] = None) {
	val logger = LoggerFactory.getLogger(getClass)
	val sqls :Map[String, String] = Map()
	val results :Map[String, Any] = Map()

	def this(url :String, user :String, password :String) = this(url, Option(user), Option(password))
	def this(url :String, user :String) = this(url, Option(user))

	def registerSql(alias :String, sql :String) = {sqls += (alias -> sql)}

	def execSqls(dbAlias :String) = {
		val cnx = user match {
			case Some(sUser) => {
				password match {
					case Some(sPassword) => DriverManager.getConnection(url, sUser, sPassword)
					case None => DriverManager.getConnection(url, sUser, null)
				}
			}
			case None => DriverManager.getConnection(url)
		}
		cnx.setAutoCommit(false)
		val stmt = cnx.createStatement
		sqls foreach {case(alias, sql) => {
			logger.info(s"$dbAlias.sql.$alias")
			val rs = stmt.executeQuery(sql)
			val obj :Any = rs.next match {
				case true => results += (alias -> rs.getObject(1))
				case false => results += (alias -> null)
			}
		}}
		cnx.close
	}

	def print() = {
		logger.info(s"url -> $url")
		user match {
			case Some(s) => logger.info(s"user -> $s")
			case None =>
		}
		password match {
			case Some(s) => logger.info(s"password -> $s")
			case None =>
		}
		sqls foreach {case(k, v) => logger.info(s"sql: $k -> $v")}
		results foreach {case(k, v) => logger.info(s"sql: $k -> $v")}
	}
}

class Querier(val fname :String) {
	protected val reFirstWord = """^(\w+)(?=(\.\w+)+$)""".r
	protected val reSql = """(?<=^\w+\.sql\.)(\w+)$""".r
	protected val reSshLogin = """((?:\w+)(?:\.\w+)*)@((?:\w+)(?:\.\w+)*)""".r
	protected val reSshPortFwd = """(?:\w+)(?:\.\w+)*:\d+:(?:\w+)(?:\.\w+)*:\d+""".r
	protected val querySets :Map[String, QuerySet] = Map()
	protected val sshSessions :Map[String, Session] = Map()

	val logger = LoggerFactory.getLogger(getClass)

	// register connection, establish SSH port-forwarding if necessary
	private def registerConnection(props :PropertiesConfiguration, dbAlias :String) = {
		Option(props.getProperty(dbAlias + ".sshLogin").asInstanceOf[String]) match {
			case Some(sSshLogin :String) => {
				sSshLogin match {
					case reSshLogin(sshUser, sshHost) => {
						val jsch = new JSch()
						Option(props.getProperty(dbAlias + ".sshKey").asInstanceOf[String]) match {
							case Some(sshKey) => {
								logger.info(s"sshKey -> $sshKey")
								Option(props.getProperty(dbAlias + ".sshKeyPassphrase").asInstanceOf[String]) match {
									case Some(sshKeyPassphrase) => jsch.addIdentity(sshKey, sshKeyPassphrase)
									case _ => jsch.addIdentity(sshKey)
								}
							}
							case None =>
						}
						Option(props.getProperty(dbAlias + ".sshKnownHosts").asInstanceOf[String]) match {
							case Some(sshKnownHosts) => {
								logger.info(s"sshKnownHosts -> $sshKnownHosts")
								jsch.setKnownHosts(sshKnownHosts)
							}
							case None =>
						}
						Option(props.getProperty(dbAlias + ".sshPortForwarding").asInstanceOf[String]) match {
							case Some(sSshPortFwd :String) => {
								logger.info(s"SSH Port-Forwarding -> $sSshPortFwd")
								val session = jsch.getSession(sshUser, sshHost, 22)
								Option(props.getProperty(dbAlias + ".sshPassword").asInstanceOf[String]) match {
									case Some(sshPassword) => {
										logger.info("sshPassword -> <REDACTED>")
										session.setPassword(sshPassword)
									}
									case _ =>
								}
								Option(props.getProperty(dbAlias + ".sshStrictHostKeyChecking").asInstanceOf[String]) match {
									case Some(sshStrictHostKeyChecking) => {
										logger.info(s"sshStrictHostKeyChecking -> $sshStrictHostKeyChecking")
										val sshConfig = new java.util.Properties()
										sshConfig.put("StrictHostKeyChecking", sshStrictHostKeyChecking)
										session.setConfig(sshConfig)
									}
									case _ =>
								}
								session.connect()
								session.setPortForwardingL(sSshPortFwd)
								registerDatabase(props, dbAlias)
								sshSessions += (dbAlias -> session)
							}
							case None => registerDatabase(props, dbAlias)
						}
					}
					case _ =>
				}
			}
			case None =>
		}
	}

	// register database connection info if we have URL, with optional user and password
	private def registerDatabase(props :PropertiesConfiguration, dbAlias :String) = {
		Option(props.getProperty(dbAlias + ".url").asInstanceOf[String]) match {
			case Some(sUrl :String) => {
				val snapshot = Option(props.getProperty(dbAlias + ".user").asInstanceOf[String]) match {
					case Some(sUser :String) => {
						Option(props.getProperty(dbAlias + ".password").asInstanceOf[String]) match {
							case Some(sPassword :String) => new QuerySet(sUrl, sUser, sPassword)
							case None => new QuerySet(sUrl, sUser)
						}
					}
					case None => new QuerySet(sUrl)
				}
				querySets += (dbAlias -> snapshot)
			}
			case None => throw new Exception(s"$dbAlias.url is missing")
		}
	}

	// register SQL to valid database alias
	private def registerSql(dbAlias :String, sqlAlias :String, sql :String) = {
		querySets.get(dbAlias) match {
			case Some(snapshot) => snapshot.registerSql(sqlAlias, sql)
			case None => throw new Exception(s"Something wrong registering database alias '$dbAlias'.")
		}
	}

	protected def prepare(props :PropertiesConfiguration) = {
		props.getKeys.foreach(k => {
			reFirstWord.findFirstIn(k) match {
				case Some(dbAlias :String) => {
					querySets.get(dbAlias) match {
						case Some(snapshot) =>
						case None => registerConnection(props, dbAlias)
					}
					reSql.findFirstIn(k) match {
						case Some(sqlAlias :String) => registerSql(dbAlias, sqlAlias, props.getString(k))
						case None =>
					}
				}
				case None =>
			}
		})
	}

	private def outputCsv(fname :String) = {
		val csvw = new CSVWriter(new FileWriter(fname))
		val sqlAliases :Set[String] = Set()
		querySets.values.foreach(v => v.sqls.keys.foreach(k => sqlAliases += k))

		var rTitle = List((new Date()).toString)
		querySets.keys.foreach(k => rTitle = rTitle :+ k)
		csvw.writeNext(rTitle.toArray)

		sqlAliases foreach {sqlAlias => {
			var r :List[String] = List()
			r = r :+ sqlAlias
			querySets foreach {case(dbAlias, snapshot) => {
				snapshot.results.get(sqlAlias) match {
					case Some(result) => r = r :+ result.toString
					case None => r = r :+ null.asInstanceOf[String]
				}
			}}
			csvw.writeNext(r.toArray)
			csvw.flush
		}}

		csvw.close
	}

	def run() = {
		val props = new PropertiesConfiguration
		props.read(new FileReader(fname))

		prepare(props)
		querySets.par.foreach{kv => {
			kv._2.execSqls(kv._1)
			sshSessions.get(kv._1) match {
				case Some(session) => session.disconnect()
				case None =>
			}
		}}
		outputCsv(sys.props.getOrElse("csv.output", "r1c1.csv"))
	}
}
