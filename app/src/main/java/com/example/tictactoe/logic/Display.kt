class Display {
    companion object{
        fun toConsole(str:Any, delay:Int = 500){
            println(str.toString())
            Thread.sleep(delay.toLong())
        }
    }
}