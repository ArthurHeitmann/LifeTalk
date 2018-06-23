package lifeTalk.jsonRW;

import java.io.Serializable;
import java.util.Date;

/**
 * Simple class to store, serialize and send messages
 * 
 * @author Arthur H.
 *
 */
public class Message implements Serializable {
	private static final long serialVersionUID = 2399767928052504699L;
	/** text content of the message */
	public String content;
	/** when the message was sent */
	public long date;
	/** The sender of the message */
	public String sender;
	/** receiver of the message */
	public String receiver;
	/** True: normal message; False: life message */
	public boolean messageSent;

	/**
	 * Saves all message details into this object.
	 * 
	 * @param cont Text content of the message
	 * @param time The time when the message was sent
	 * @param from The sender of the message
	 * @param to The receiver of the message
	 * @param sentMessage whether this is a normal message or a life message
	 */
	public Message(String cont, long time, String from, String to, boolean sentMessage) {
		content = cont;
		date = time;
		sender = from;
		receiver = to;
		messageSent = sentMessage;
	}

	@Override
	public String toString() {
		return "MESSAGE[ from: " + sender + "; to: " + receiver + "; content: " + content + "; dateTime: " + new Date(date).toString() + " ]";
	}
}
