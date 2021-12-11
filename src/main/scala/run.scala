object R1C1 extends App {
	args.foreach(arg => {
		val querier = new com.shinkou.r1c1.Querier(arg);
		querier.run()
	})
}
