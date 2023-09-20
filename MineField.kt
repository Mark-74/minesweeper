import kotlin.random.Random

class MineField(private val mode:Int, private var bombs: Int) {

    init{

        require(mode > 0){
            "You can't play in a grid smaller than 1 x 1"
        }
        require(bombs in 1 until mode*mode){
            "There cannot be more bombs than cells or less than 1 bomb"
        }
    }

    private val grid = Array<Char> (mode*mode) {'*'}
    private var flagsDeployed = 0
    private val flagsGrid = Array<Char> (mode*mode) {'*'}

    private val red = "\u001b[31m"
    private val green = "\u001b[32m"
    private val blue = "\u001b[34m"
    private val brown = "\u001b[33m"
    private val purple = "\u001b[35m"
    private val reset = "\u001b[0m"

    private var playing = true

    private fun addBombs(){
        var i = 0

        while(i<bombs) {

            val bombPosition =  Random.nextInt(0,mode*mode)
            if(grid[bombPosition] != 'b'){
                grid[bombPosition] = 'b'
                i ++
            }
        }
    }

    private fun flag(cell:Int){

        if(grid[cell]!='*' && grid[cell] !='b'){
            return
        } else {
            flagsGrid[cell] = 'f'
            flagsDeployed ++
        }
    }

    private fun unflag(cell:Int){

        if(flagsGrid[cell]!='f'){
            return
        } else {
            flagsGrid[cell] = '*'
            flagsDeployed --
        }
    }

    private fun open(cell:Int){

        if(grid[cell] != 'b'){
                val bombsNumber = bombCheck(cell)
                if (bombsNumber == 0) {
                    cleanAdjacentCells(cell)
                } else {
                    grid[cell] = bombsNumber.digitToChar()
                }
        } else {
            lose()
        }
    }

    private fun bombCheck(cell: Int): Int{ //numero delle bombe nelle caselle circostanti

        var bombsNumber = 0

        var checkLeft = true
        for(i in 0..mode*(mode-1) step mode){
            if(cell == i){
                checkLeft = false
            }
        }

        var checkRight = true
        for(i in (mode-1)until mode*mode step mode){
            if(cell == i){
                checkRight = false
            }
        }

        var checkTop = true
        for(i in 0 until mode){
            if(cell == i){
                checkTop = false
            }
        }

        var checkBottom = true
        for(i in mode*(mode-1) until mode*mode){
            if(cell == i){
                checkBottom = false
            }
        }

        if(checkLeft){
            if(grid[cell-1] == 'b'){bombsNumber ++}
        }

        if(checkTop && checkRight){ // guarda la casella in alto a dx
            if(grid[cell-(mode-1)] == 'b'){bombsNumber ++}
        }

        if(checkRight){
            if(grid[cell+1] == 'b'){bombsNumber ++}
        }

        if(checkTop){
            if(grid[cell-mode] == 'b'){bombsNumber ++}

            if(checkLeft){ // guarda la casella in alto a sx
                if(grid[cell-mode-1] == 'b'){bombsNumber ++}
            }
        }

        if(checkBottom){
            if(grid[cell+mode] == 'b'){bombsNumber ++}

            if(checkRight){ // guarda la casella in basso a dx
                if(grid[cell+mode+1] == 'b'){bombsNumber ++}
            }
        }

        if(checkBottom && checkLeft){ // guarda la casella in basso a sx
            if(grid[cell+mode-1] == 'b'){bombsNumber ++}
        }

        return bombsNumber
    }

    private fun cleanAdjacentCells(cell:Int){

        if (cell !in grid.indices ){ return } //controllo che la cella sia dentro la mappa

        if(grid[cell] == 'b' || grid[cell] != '*') { return } //controllo che la cella non sia già stata aperta

        val adjacentBombs = bombCheck(cell)

        if( adjacentBombs == 0){

            grid[cell] = 'x'

            if(cell !in 0..mode*(mode-1) step mode){ //controllo sx
                cleanAdjacentCells(cell-(mode+1))
                cleanAdjacentCells(cell-1)
                cleanAdjacentCells(cell+(mode-1))
            }

            if(cell !in mode-1 until mode*mode-1 step mode){ //controllo dx
                cleanAdjacentCells(cell+(mode+1))
                cleanAdjacentCells(cell+1)
                cleanAdjacentCells(cell-(mode-1))
            }

            cleanAdjacentCells(cell-mode)
            cleanAdjacentCells(cell+mode)

        } else {
            grid[cell] = adjacentBombs.digitToChar()
        }
    }

    private fun lose(){

        playing = false
        println(red + "You lost!" + reset)
    }

    fun interact(row: Int, column: Int, whatToDo: String){

        val cell = column + row * mode

        if(cell !in 0..mode*mode){
            println("the cell $cell is not inside the grid")
            return
        }

        when (whatToDo) {
            "flag" -> {
                flag(cell)
            }
            "open" -> {
                open(cell)
            }
            "unflag" -> {
                unflag(cell)
            }
        }
    }

    private fun showMap(){

        for(i in 0 until mode){
            print(i)
            print("   ")
            for(j in 0 until mode) {

                // print(grid[i * mode + j] + " ") per test

                if (flagsGrid[i * mode + j] == 'f') {
                    print(red + "f " + reset)
                } else if (grid[i * mode + j] == 'b') {
                    print(green + "* " + reset)
                }else if(grid[i*mode+j] == 'x'){
                    print(brown + grid[i * mode + j] + " " + reset)
                }else if(grid[i*mode+j] != '*') {
                    print(blue + grid[i * mode + j] + " " + reset)
                } else {
                    print(green + grid[i * mode + j] + " " + reset)
                }

            }
            println()
        }

        println()
        print("    ")
        for(i in 0 until mode){
            print(i)
            print(" ")
        }

        println()
        println("flags : ${flagsDeployed}/${bombs}")
    }

    private fun reset(){

        for(i in grid.indices){
            grid[i] = '*'
        }
        for(i in flagsGrid.indices){
            grid[i] = '*'
        }
    }

    private fun win(){

        println(purple + "You won!" + reset)

        playing = false
    }

    private fun checkWin(){

        var win = true
        for (i in grid.indices){
            if(grid[i]=='b' && flagsGrid[i] != 'f'){ win = false }
        }

        if(win){ win() }
    }

    fun play(){

        reset()
        addBombs()

        println("Flag all the bombs to win!")

        while(playing){

            showMap()
            println("row:")
            val row:Int = readLine()!!.toInt()

            println("column:")
            val column = readLine()!!.toInt()

            println("what to do:")
            val whatToDo = readLine().toString()

            interact(row, column, whatToDo)

            checkWin()
        }
    }
}