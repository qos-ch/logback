package ch.qos.logback.core.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.spi.ContextAwareBase;

public class NetworkAddressUtil extends ContextAwareBase {

	public NetworkAddressUtil(final Context context) {
		setContext(context);
	}

	public static String getLocalHostName() throws UnknownHostException, SocketException {
		try {
			final InetAddress localhost = InetAddress.getLocalHost();
			return localhost.getHostName();
		} catch (final UnknownHostException e) {
			return getLocalAddressAsString();
		}
	}

	public static String getCanonicalLocalHostName() throws UnknownHostException, SocketException {
		try {
			final InetAddress localhost = InetAddress.getLocalHost();
			return localhost.getCanonicalHostName();
		} catch (final UnknownHostException e) {
			return getLocalAddressAsString();
		}
	}

	private static String getLocalAddressAsString() throws UnknownHostException, SocketException {
		final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
		while (interfaces != null && interfaces.hasMoreElements()) {
			final Enumeration<InetAddress> addresses = interfaces.nextElement().getInetAddresses();
			while (addresses != null && addresses.hasMoreElements()) {
				final InetAddress address = addresses.nextElement();
				if (acceptableAddress(address)) {
					return address.getHostAddress();
				}
			}
		}
		throw new UnknownHostException();
	}

	private static boolean acceptableAddress(final InetAddress address) {
		return address != null && !address.isLoopbackAddress() && !address.isAnyLocalAddress() && !address.isLinkLocalAddress();
	}

	/**
	 * Add the local host's name as a property
	 */
	 public String safelyGetLocalHostName() {
		try {
			return getLocalHostName();
		} catch (UnknownHostException | SocketException | SecurityException e) {
			addError("Failed to get local hostname", e);
		}
		return CoreConstants.UNKNOWN_LOCALHOST;
	 }

	 public String safelyGetCanonicalLocalHostName() {
		 try {
			 return getCanonicalLocalHostName();
		 } catch (UnknownHostException | SocketException | SecurityException e) {
			 addError("Failed to get canonical local hostname", e);
		 }
		 return CoreConstants.UNKNOWN_LOCALHOST;

	 }

}
