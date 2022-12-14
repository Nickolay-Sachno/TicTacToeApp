package com.example.tictactoe.settings

import Cell
import CellType
import GameState
import Grid
import Player
import User

// toString() : GameStateBridge(grid=GridBridge(dim=3, matrix=[[CellBridge(row=0, col=0, content=CROSS), CellBridge(row=0, col=1, content=EMPTY), CellBridge(row=0, col=2, content=EMPTY)], [CellBridge(row=1, col=0, content=EMPTY), CellBridge(row=1, col=1, content=EMPTY), CellBridge(row=1, col=2, content=EMPTY)], [CellBridge(row=2, col=0, content=EMPTY), CellBridge(row=2, col=1, content=EMPTY), CellBridge(row=2, col=2, content=EMPTY)]]), notVisitedCell=CellBridge(row=-1, col=-1, content=EMPTY), listOfPlayers=[PlayerBridge(cellType=CIRCLE), PlayerBridge(cellType=CROSS)], listOfMoves=[(0, 0)])

data class GameStateBridge(
    val grid: GridBridge,
    val notVisitedCell: CellBridge,
    val listOfPlayers: MutableList<PlayerBridge>,
    val listOfMoves: MutableList<Pair<Int, Int>>,
){
    fun getGameStateBridgeFromGameState(): GameState{
        return GameState(
            grid = Grid(
                dim = 3,
                matrix = Array(3) { row ->
                    Array(3) { col ->
                        Cell(
                            row = row,
                            col = col,
                            content = this.grid.matrix[row][col].content
                        )
                    }
                }
            ),
            listOfPlayers = mutableListOf<Player>().apply {
                add(User(this@GameStateBridge.listOfPlayers[0].cellType))
                add(User(this@GameStateBridge.listOfPlayers[1].cellType))
            },
            listOfMoves = this.listOfMoves,
            notVisitedCell = Cell(content = this.notVisitedCell.content)
        )
    }
}

data class CellBridge(
    val row : Int = -1,
    val col : Int = -1,
    var content : CellType = CellType.EMPTY
)

data class PlayerBridge(
    val cellType: CellType
)

data class GridBridge(
    val dim: Int,
    var matrix: List<List<CellBridge>>
)
