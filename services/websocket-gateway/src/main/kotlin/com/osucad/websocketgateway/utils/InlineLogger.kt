package com.osucad.websocketgateway.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory

inline fun <reified T : Any> T.inlineLogger(): Logger = LoggerFactory.getLogger(T::class.java)
