package com.osucad.protocol

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("connect_info")
data class ConnectInfo(val clientId: String) : ServerMessage