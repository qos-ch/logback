package ch.qos.logback.classic.net;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.layout.EchoLayout;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.Message;


/**
 * XMMP (Jabber / GoogleHangout) async appender
 * <p>It is recommended to wrap it with AsyncAppender</p>
 * @see ch.qos.logback.classic.AsyncAppender
 * @author szalik
 */
public class XmppAppender<E> extends AppenderBase<E> {
	private ConnectionConfiguration connectionConfiguration;
	private XMPPConnection conn;
	private String password;
	private String username;
	private String resourceName = getClass().getSimpleName();
	private String sendToJid;
	private Chat chat;
	private int xmmpPort = 5222;
	private String xmmpServer;
	private Layout<E> layout = new EchoLayout<E>();


	public void setSendToJid(String sendToJid) {
		this.sendToJid = sendToJid;
	}

	public void setLayout(Layout<E> layout) {
		this.layout = layout;
	}

	/**
	 * @param xmmpAccount jid@server.org[:port]
	 */
	public void setXmmpAccount(String xmmpAccount) {
		String[] parts = xmmpAccount.split(":", 2);
		if (parts.length == 2) {
			xmmpPort = Integer.parseInt(parts[1].trim());
		}
		parts = parts[0].split("@", 2);
		username = parts[0];
		xmmpServer = parts[1];
	}


	public void setUsername(String username) {
		this.username = username;
	}


	public void setPassword(String password) {
		this.password = password;
	}


	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}


	@Override
	public void stop() {
        super.stop();
		boolean doStop = isStarted();
		super.stop();
		if (doStop) {
			if (conn != null && conn.isConnected()) {
				conn.disconnect();
				chat = null;
			}
		}
	}

    @Override
    public void start() {
        Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.accept_all);
        connectionConfiguration = new ConnectionConfiguration(xmmpServer, xmmpPort);
        try {
            xmmpConnect();
        } catch (XMPPException e) {
            addWarn(formatLogMessage("Unable to connect to xmmp server!"), e);
        }
        super.start();
    }


    @Override
    protected void append(E event) {
        if (chat == null) {
            addInfo(formatLogMessage("Connecting to xmmp server - " + xmmpServer + ":" + xmmpPort));
            try {
                xmmpConnect();
            } catch (XMPPException e) {
			    /* ignore */
            }
        }
        if (chat != null) {
            Message message = new Message(sendToJid);
            message.setBody(layout.doLayout(event));
            try {
                chat.sendMessage(message);
            } catch (XMPPException e) {
                addError(formatLogMessage("Error sending message to " + sendToJid + "."), e);
            }
        }
    }


    private synchronized void xmmpConnect() throws XMPPException {
		try {
			XMPPConnection conn = new XMPPConnection(connectionConfiguration);
			conn.connect();
			conn.login(username, password, resourceName);
			ChatManager chatmanager = conn.getChatManager();
			Chat chat = chatmanager.createChat(sendToJid, new MessageListener() {
				@Override
				public void processMessage(Chat chat, Message msg) { /* ignore incoming messages */	}
			});
			Roster roster = conn.getRoster();
			if (!roster.contains(sendToJid)) {
				addInfo(formatLogMessage("Adding '" + sendToJid + "' to roster."));
				roster.createEntry(sendToJid, sendToJid, new String[] {});
			}
			this.chat = chat;
			this.conn = conn;
		} catch (XMPPException e) {
			this.chat = null;
			this.conn = null;
			addError(formatLogMessage("Error connecting to " + xmmpServer + ':' + xmmpPort), e);
			throw e;
		}
	}

    private String formatLogMessage(String logMessage) {
        return "Appender " + getName() + ": " + logMessage;
    }

}
