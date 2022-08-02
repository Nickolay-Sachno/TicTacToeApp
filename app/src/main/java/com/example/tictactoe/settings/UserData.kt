package com.example.tictactoe.settings

import Player
import User

data class UserData(
    override var player: Player = User(
        cellType = CellType.CROSS
    ),
    override var cellTypeImg: CellTypeImg = CellTypeImg.CIRCLE_BLACK
    ) : PlayerData
