/** This class represents a cell in the grid of TicTacToe */
class Cell (val row : Int = -1, val col : Int = -1, var content : CellType = CellType.EMPTY){

    fun clear(){
        this.content = CellType.EMPTY
    }

    override fun equals(other: Any?): Boolean {
        return if(other is Cell)
            this.content == other.content
        else
            super.equals(other)
    }

    override fun toString(): String {
        return this.content.chr.toString()
    }

    fun deepCopy() : Cell{
        return Cell(
            row = this.row,
            col = this.col,
            content = this.content
        )
    }
}