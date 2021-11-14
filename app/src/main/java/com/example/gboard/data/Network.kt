package com.example.gboard.data

import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.ByteBuffer

object Network {
	private var rHandlerField: Handler? = null
	private val rHandler: Handler
		get() {
			if (rHandlerField == null) {
				if (rHandlerThread == null) {
					rHandlerThread = HandlerThread("DataReceiver")
					rHandlerThread!!.start()
				}
				rHandlerField = Handler(rHandlerThread!!.looper)
			}
			return rHandlerField!!
		}
	private var rHandlerThread: HandlerThread? = null

	private var tHandlerField: Handler? = null
	private val tHandler: Handler
		get() {
			if (tHandlerField == null) {
				if (tHandlerThread == null) {
					tHandlerThread = HandlerThread("DataTransmitter")
					tHandlerThread!!.start()
				}
				tHandlerField = Handler(tHandlerThread!!.looper)
			}
			return tHandlerField!!
		}
	private var tHandlerThread: HandlerThread? = null


	val receivePacket = OnChanger<DatagramPacket>(DatagramPacket(ByteArray(1), 1))
	val ping = OnChanger<Long>(0L)
	val startGameFlag = OnChanger<Byte>(0)

	const val CONNECTION_ESTABLISHED: Byte = 124
	const val CONNECTION_STARTED: Byte = 125
	const val CONNECTION_REFUSED: Byte = 126
	const val CONNECTION_END: Byte = 127

	const val GAME_STARTED: Byte = 123
	const val SNAKE_DIRECTION: Byte = 122

	private var socket: DatagramSocket? = null
	private var address = InetAddress.getByName(/*"192.168.1.5"*/"95.142.45.201")
	private var port = 4444
	private var sendPacketTime = 0L

	fun sendDirection(cos: Float, sin: Float) {
		val cosBytes = ByteBuffer.allocate(4).putFloat(cos).array()
		val sinBytes = ByteBuffer.allocate(4).putFloat(sin).array()
		val flag = byteArrayOf(SNAKE_DIRECTION)
		val msg = flag + cosBytes + sinBytes
		tHandler.post {
			socket?.send(DatagramPacket(msg, 9, address, port))
		}
	}

	fun sendRequestToServer() {
		if (socket == null) {
			socket = DatagramSocket()
		}
		tHandler.post {
			socket?.send(DatagramPacket(byteArrayOf(CONNECTION_STARTED), 1, address, port))
			sendPacketTime = System.currentTimeMillis()
		}
	}

	fun sendStartFlag() {
		tHandler.post {
			socket?.send(DatagramPacket(byteArrayOf(GAME_STARTED), 1, address, port))
		}
	}

	fun waitGameStartFlag() {
		rHandler.post {
			val packet = DatagramPacket(ByteArray(1), 1)
			socket?.receive(packet)
			val bytes = packet.data
			if (bytes[0] == GAME_STARTED)
				startGameFlag.value = bytes[0]
		}
	}

	fun receiveMessage() {
		rHandler.post {
			val packet = DatagramPacket(ByteArray(100), 100)
			socket?.receive(packet)
			receivePacket.value = packet
			ping.value = System.currentTimeMillis() - sendPacketTime
		}
	}

	fun disconnect() {
		tHandler.post {
			socket?.send(DatagramPacket(byteArrayOf(CONNECTION_END), 1, address, port))
			socket = null
		}
		receivePacket.onChange.clear()
		ping.onChange.clear()
	}
}