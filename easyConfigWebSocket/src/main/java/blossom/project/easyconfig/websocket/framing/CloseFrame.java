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

package blossom.project.easyconfig.websocket.framing;

import blossom.project.easyconfig.websocket.exceptions.InvalidDataException;
import blossom.project.easyconfig.websocket.exceptions.InvalidFrameException;
import blossom.project.easyconfig.websocket.util.ByteBufferUtils;
import blossom.project.easyconfig.websocket.util.Charsetfunctions;

import java.nio.ByteBuffer;

/**
 * Class to represent a close frame
 */
public class CloseFrame extends ControlFrame {

    /**
     * indicates a normal closure, meaning whatever purpose the
     * connection was established for has been fulfilled.
     */
    public static final int NORMAL = 1000;
    /**
     * 1001 indicates that an endpoint is "going away", such as a server
     * going down, or a browser having navigated away from a page.
     */
    public static final int GOING_AWAY = 1001;
    /**
     * 1002 indicates that an endpoint is terminating the connection due
     * to a protocol error.
     */
    public static final int PROTOCOL_ERROR = 1002;
    /**
     * 1003 indicates that an endpoint is terminating the connection
     * because it has received a type of data it cannot accept (e.g. an
     * endpoint that understands only text data MAY send this if it
     * receives a binary message).
     */
    public static final int REFUSE = 1003;
    /*1004: Reserved. The specific meaning might be defined in the future.*/
    /**
     * 1005 is a reserved value and MUST NOT be set as a status code in a
     * Close control frame by an endpoint. It is designated for use in
     * applications expecting a status code to indicate that no status
     * code was actually present.
     */
    public static final int NOCODE = 1005;
    /**
     * 1006 is a reserved value and MUST NOT be set as a status code in a
     * Close control frame by an endpoint. It is designated for use in
     * applications expecting a status code to indicate that the
     * connection was closed abnormally, e.g. without sending or
     * receiving a Close control frame.
     */
    public static final int ABNORMAL_CLOSE = 1006;
    /**
     * 1007 indicates that an endpoint is terminating the connection
     * because it has received data within a message that was not
     * consistent with the type of the message (e.g., non-UTF-8 [RFC3629]
     * data within a text message).
     */
    public static final int NO_UTF8 = 1007;
    /**
     * 1008 indicates that an endpoint is terminating the connection
     * because it has received a message that violates its policy. This
     * is a generic status code that can be returned when there is no
     * other more suitable status code (e.g. 1003 or 1009), or if there
     * is a need to hide specific details about the policy.
     */
    public static final int POLICY_VALIDATION = 1008;
    /**
     * 1009 indicates that an endpoint is terminating the connection
     * because it has received a message which is too big for it to
     * process.
     */
    public static final int TOOBIG = 1009;
    /**
     * 1010 indicates that an endpoint (client) is terminating the
     * connection because it has expected the server to negotiate one or
     * more extension, but the server didn't return them in the response
     * message of the WebSocket handshake. The list of extensions which
     * are needed SHOULD appear in the /reason/ part of the Close frame.
     * Note that this status code is not used by the server, because it
     * can fail the WebSocket handshake instead.
     */
    public static final int EXTENSION = 1010;
    /**
     * 1011 indicates that a server is terminating the connection because
     * it encountered an unexpected condition that prevented it from
     * fulfilling the request.
     **/
    public static final int UNEXPECTED_CONDITION = 1011;
    /**
     * 1015 is a reserved value and MUST NOT be set as a status code in a
     * Close control frame by an endpoint. It is designated for use in
     * applications expecting a status code to indicate that the
     * connection was closed due to a failure to perform a TLS handshake
     * (e.g., the server certificate can't be verified).
     **/
    public static final int TLS_ERROR = 1015;

    /**
     * The connection had never been established
     */
    public static final int NEVER_CONNECTED = -1;

    /**
     * The connection had a buggy close (this should not happen)
     */
    public static final int BUGGYCLOSE = -2;

    /**
     * The connection was flushed and closed
     */
    public static final int FLASHPOLICY = -3;


    /**
     * The close code used in this close frame
     */
    private int code;

    /**
     * The close message used in this close frame
     */
    private String reason;

    /**
     * Constructor for a close frame
     * <p>
     * Using opcode closing and fin = true
     */
    public CloseFrame() {
        super(Opcode.CLOSING);
        setReason("");
        setCode(CloseFrame.NORMAL);
    }

	/**
	 * Set the close code for this close frame
	 * @param code the close code
	 */
	public void setCode(int code) {
        this.code = code;
        // CloseFrame.TLS_ERROR is not allowed to be transfered over the wire
        if (code == CloseFrame.TLS_ERROR) {
            this.code = CloseFrame.NOCODE;
            this.reason = "";
        }
        updatePayload();
    }

	/**
	 * Set the close reason for this close frame
	 * @param reason the reason code
	 */
    public void setReason(String reason) {
        if (reason == null) {
            reason = "";
        }
        this.reason = reason;
        updatePayload();
    }
    /**
     * Get the used close code
     *
     * @return the used close code
     */
    public int getCloseCode() {
        return code;
    }

    /**
     * Get the message that closeframe is containing
     *
     * @return the message in this frame
     */
    public String getMessage() {
        return reason;
    }

    @Override
    public String toString() {
        return super.toString() + "code: " + code;
    }

    @Override
    public void isValid() throws InvalidDataException {
        super.isValid();
        if (code == CloseFrame.NO_UTF8 && reason == null) {
        	throw new InvalidDataException( CloseFrame.NO_UTF8 );
		}
        if (code == CloseFrame.NOCODE && 0 < reason.length()) {
            throw new InvalidDataException(PROTOCOL_ERROR, "A close frame must have a closecode if it has a reason");
        }
        //Intentional check for code != CloseFrame.TLS_ERROR just to make sure even if the code earlier changes
        if ((code > CloseFrame.UNEXPECTED_CONDITION && code < 3000 && code != CloseFrame.TLS_ERROR)) {
            throw new InvalidDataException(PROTOCOL_ERROR, "Trying to send an illegal close code!");
        }
        if (code == CloseFrame.ABNORMAL_CLOSE || code == CloseFrame.TLS_ERROR || code == CloseFrame.NOCODE || code > 4999 || code < 1000 || code == 1004) {
            throw new InvalidFrameException("closecode must not be sent over the wire: " + code);
        }
    }

    @Override
    public void setPayload(ByteBuffer payload) {
		code = CloseFrame.NOCODE;
		reason = "";
		payload.mark();
		if( payload.remaining() == 0 ) {
			code = CloseFrame.NORMAL;
		} else if( payload.remaining() == 1 ) {
			code = CloseFrame.PROTOCOL_ERROR;
		} else {
			if( payload.remaining() >= 2 ) {
				ByteBuffer bb = ByteBuffer.allocate( 4 );
				bb.position( 2 );
				bb.putShort( payload.getShort() );
				bb.position( 0 );
				code = bb.getInt();
			}
			payload.reset();
			try {
				int mark = payload.position();// because stringUtf8 also creates a mark
				try {
					payload.position( payload.position() + 2 );
					reason = Charsetfunctions.stringUtf8( payload );
				} catch ( IllegalArgumentException e ) {
					throw new InvalidDataException( CloseFrame.NO_UTF8 );
				} finally {
					payload.position( mark );
				}
			} catch ( InvalidDataException e ) {
				code = CloseFrame.NO_UTF8;
				reason = null;
			}
		}
	}

    /**
     * Update the payload to represent the close code and the reason
     */
    private void updatePayload() {
        byte[] by = Charsetfunctions.utf8Bytes(reason);
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.putInt(code);
        buf.position(2);
        ByteBuffer pay = ByteBuffer.allocate(2 + by.length);
        pay.put(buf);
        pay.put(by);
        pay.rewind();
        super.setPayload(pay);
    }

    @Override
    public ByteBuffer getPayloadData() {
        if (code == NOCODE)
            return ByteBufferUtils.getEmptyByteBuffer();
        return super.getPayloadData();
    }

}
