package lifeTalk.jsonRW;

import java.io.Serializable;
import java.util.Date;

public class Message implements Serializable {
	private static final long serialVersionUID = 2399767928052504699L;
	public String content;
	public long date;
	public String sender;
	public String receiver;
	public boolean messageSent;

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
