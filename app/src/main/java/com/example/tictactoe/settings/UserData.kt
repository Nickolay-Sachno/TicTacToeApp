package com.example.tictactoe.settings

import Player
import User

data class UserData(
    override var player: Player = User(
        cellType = CellType.CROSS
    ),
    override var cellTypeImg: CellTypeImg = CellTypeImg.CROSS_BLACK,
    override var winCellTypeImg: CellTypeImg = CellTypeImg.CROSS_RED,
    override var listOfWinMoves: MutableList<Pair<Int, Int>> = mutableListOf()
) : PlayerData
