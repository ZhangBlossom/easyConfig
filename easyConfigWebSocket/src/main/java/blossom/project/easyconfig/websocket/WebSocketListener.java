/*
 * Copyright (c) 2010-2023 Nathan Rajlich
 *
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining a copy of this software and associated documentation
 *  files (the "Software"), to deal in the Software without
 *  restriction, including without limitation the rights to use,
 *  copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following
 *  conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE.
 */

package blossom.project.easyconfig.websocket;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import blossom.project.easyconfig.websocket.drafts.Draft;
import blossom.project.easyconfig.websocket.exceptions.InvalidDataException;
import blossom.project.easyconfig.websocket.framing.CloseFrame;
import blossom.project.easyconfig.websocket.framing.Framedata;
import blossom.project.easyconfig.websocket.handshake.ClientHandshake;
import blossom.project.easyconfig.websocket.handshake.Handshakedata;
import blossom.project.easyconfig.websocket.handshake.ServerHandshake;
import blossom.project.easyconfig.websocket.handshake.ServerHandshakeBuilder;

/**
 * Implemented by <tt>WebSocketClient</tt> and <tt>WebSocketServer</tt>.
 * The methods within are called by <tt>WebSocket</tt>.
 * Almost every method takes a first parameter conn which represents the source of the respective event.
 */
public interface WebSocketListener {

	/**
	 * Called on the server side when the socket connection is first established, and the WebSocket
	 * handshake has been received. This method allows to deny connections based on the received handshake.<br>
	 * By default this method only requires protocol compliance.
	 * 
	 * @param conn
	 *            The WebSocket related to this event
	 * @param draft
	 *            The protocol draft the client uses to connect
	 * @param request
	 *            The opening http message send by the client. Can be used to access additional fields like cookies.
	 * @return Returns an incomplete handshake containing all optional fields
	 * @throws InvalidDataException
	 *             Throwing this exception will cause this handshake to be rejected
	 */
	public ServerHandshakeBuilder onWebsocketHandshakeReceivedAsServer(WebSocket conn, Draft draft,
                                                                       ClientHandshake request) throws InvalidDataException;

	/**
	 * Called on the client side when the socket connection is first established, and the WebSocketImpl
	 * handshake response has been received.
	 * 
	 * @param conn
	 *            The WebSocket related to this event
	 * @param request
	 *            The handshake initially send out to the server by this websocket.
	 * @param response
	 *            The handshake the server sent in response to the request.
	 * @throws InvalidDataException
	 *             Allows the client to reject the connection with the server in respect of its handshake response.
	 */
	public void onWebsocketHandshakeReceivedAsClient(WebSocket conn, ClientHandshake request, ServerHandshake response) throws InvalidDataException;

	/**
	 * Called on the client side when the socket connection is first established, and the WebSocketImpl
	 * handshake has just been sent.
	 * 
	 * @param conn
	 *            The WebSocket related to this event
	 * @param request
	 *            The handshake sent to the server by this websocket
	 * @throws InvalidDataException
	 *             Allows the client to stop the connection from progressing
	 */
	public void onWebsocketHandshakeSentAsClient(WebSocket conn, ClientHandshake request) throws InvalidDataException;

	/**
	 * Called when an entire text frame has been received. Do whatever you want
	 * here...
	 * 
	 * @param conn
	 *            The <tt>WebSocket</tt> instance this event is occurring on.
	 * @param message
	 *            The UTF-8 decoded message that was received.
	 */
	public void onWebsocketMessage(WebSocket conn, String message);

	/**
	 * Called when an entire binary frame has been received. Do whatever you want
	 * here...
	 * 
	 * @param conn
	 *            The <tt>WebSocket</tt> instance this event is occurring on.
	 * @param blob
	 *            The binary message that was received.
	 */
	public void onWebsocketMessage(WebSocket conn, ByteBuffer blob);

	/**
	 * Called when a frame fragment has been recieved
	 *
	 * @param conn
	 *            The <tt>WebSocket</tt> instance this event is occurring on.
	 * @param frame The fragmented frame
	 */
	public void onWebsocketMessageFragment(WebSocket conn, Framedata frame);

	/**
	 * Called after <var>onHandshakeReceived</var> returns <var>true</var>.
	 * Indicates that a complete WebSocket connection has been established,
	 * and we are ready to send/receive data.
	 * 
	 * @param conn The <tt>WebSocket</tt> instance this event is occuring on.
	 * @param d The handshake of the websocket instance
	 */
	public void onWebsocketOpen(WebSocket conn, Handshakedata d);

	/**
	 * Called after <tt>WebSocket#close</tt> is explicity called, or when the
	 * other end of the WebSocket connection is closed.
	 * 
	 * @param ws The <tt>WebSocket</tt> instance this event is occuring on.
	 * @param code The codes can be looked up here: {@link CloseFrame}
	 * @param reason Additional information string
	 * @param remote Returns whether or not the closing of the connection was initiated by the remote host.
	 */
	public void onWebsocketClose(WebSocket ws, int code, String reason, boolean remote);

	/** Called as soon as no further frames are accepted
	 *
	 * @param ws The <tt>WebSocket</tt> instance this event is occuring on.
	 * @param code The codes can be looked up here: {@link CloseFrame}
	 * @param reason Additional information string
	 * @param remote Returns whether or not the closing of the connection was initiated by the remote host.
	 */
	public void onWebsocketClosing(WebSocket ws, int code, String reason, boolean remote);

	/** send when this peer sends a close handshake
	 *
	 * @param ws The <tt>WebSocket</tt> instance this event is occuring on.
	 * @param code The codes can be looked up here: {@link CloseFrame}
	 * @param reason Additional information string
	 */
	public void onWebsocketCloseInitiated(WebSocket ws, int code, String reason);

	/**
	 * Called if an exception worth noting occurred.
	 * If an error causes the connection to fail onClose will be called additionally afterwards.
	 *
	 * @param conn The <tt>WebSocket</tt> instance this event is occuring on.
	 * @param ex
	 *            The exception that occurred. <br>
	 *            Might be null if the exception is not related to any specific connection. For example if the server port could not be bound.
	 */
	public void onWebsocketError(WebSocket conn, Exception ex);

	/**
	 * Called a ping frame has been received.
	 * This method must send a corresponding pong by itself.
	 *
	 * @param conn The <tt>WebSocket</tt> instance this event is occuring on.
	 * @param f The ping frame. Control frames may contain payload.
	 */
	public void onWebsocketPing(WebSocket conn, Framedata f);

	/**
	 * Called when a pong frame is received.
	 *
	 * @param conn The <tt>WebSocket</tt> instance this event is occuring on.
	 * @param f The pong frame. Control frames may contain payload.
	 **/
	public void onWebsocketPong(WebSocket conn, Framedata f);

	/**
	 * @see WebSocketAdapter#getFlashPolicy(WebSocket)
	 * @param conn The <tt>WebSocket</tt> instance this event is occuring on.
	 * @throws InvalidDataException thrown when some data that is required to generate the flash-policy like the websocket local port could not be obtained.
	 * @return An XML String that comforts to Flash's security policy. You MUST not include the null char at the end, it is appended automatically.
	 */
	public String getFlashPolicy(WebSocket conn) throws InvalidDataException;

	/** This method is used to inform the selector thread that there is data queued to be written to the socket.
	 * @param conn The <tt>WebSocket</tt> instance this event is occuring on.
	 */
	public void onWriteDemand(WebSocket conn);

	/**
	 * @see  WebSocket#getLocalSocketAddress()
	 *
	 * @param conn The <tt>WebSocket</tt> instance this event is occuring on.
	 * @return Returns the address of the endpoint this socket is bound to.
	 */
	public InetSocketAddress getLocalSocketAddress(WebSocket conn);

	/**
	 * @see  WebSocket#getRemoteSocketAddress()
	 *
	 * @param conn The <tt>WebSocket</tt> instance this event is occuring on.
	 * @return Returns the address of the endpoint this socket is connected to, or{@code null} if it is unconnected.
	 */
	public InetSocketAddress getRemoteSocketAddress(WebSocket conn);
}
