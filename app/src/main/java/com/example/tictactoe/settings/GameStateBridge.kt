package com.example.tictactoe.settings

import CellType

// toString() : GameStateBridge(grid=GridBridge(dim=3, matrix=[[CellBridge(row=0, col=0, content=CROSS), CellBridge(row=0, col=1, content=EMPTY), CellBridge(row=0, col=2, content=EMPTY)], [CellBridge(row=1, col=0, content=EMPTY), CellBridge(row=1, col=1, content=EMPTY), CellBridge(row=1, col=2, content=EMPTY)], [CellBridge(row=2, col=0, content=EMPTY), CellBridge(row=2, col=1, content=EMPTY), CellBridge(row=2, col=2, content=EMPTY)]]), notVisitedCell=CellBridge(row=-1, col=-1, content=EMPTY), listOfPlayers=[PlayerBridge(cellType=CIRCLE), PlayerBridge(cellType=CROSS)], listOfMoves=[(0, 0)])

data class GameStateBridge(
    val grid: GridBridge,
    val notVisitedCell: CellBridge,
    val listOfPlayers: MutableList<PlayerBridge>,
    val listOfMoves: MutableList<Pair<Int, Int>>,
)

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
