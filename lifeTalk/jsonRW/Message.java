package lifeTalk.jsonRW;

import java.io.Serializable;
import java.util.Date;

public class Message implements Serializable {
	private static final long serialVersionUID = 2399767928052504699L;
	public String content;
	public long date;
	public String sender;
	public String receiver;

	public Message(String cont, long time, String from, String to) {
		content = cont;
		date = time;
		sender = from;
		receiver = to;
	}

	@Override
	public String toString() {
		return "MESSAGE[ from: " + sender + "; to: " + receiver + "; content: " + content + "; dateTime: " + new Date(date).toString() + " ]";
	}
}
