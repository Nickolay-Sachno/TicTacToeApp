package com.example.tictactoe.settings

import Player

interface PlayerData {
    var player : Player
    var cellTypeImg : CellTypeImg
    var winCellTypeImg : CellTypeImg
    var listOfWinMoves : MutableList<Pair<Int, Int>>
}