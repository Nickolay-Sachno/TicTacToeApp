/** Simple enum class to control the content of each cell */
enum class CellType(var chr : Char) {
    EMPTY('-'),
    CROSS('X'),
    CIRCLE('O')
}