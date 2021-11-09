package ch.qos.logback.core.property;

import ch.qos.logback.core.PropertyDefinerBase;
import ch.qos.logback.core.util.NetworkAddressUtil;

public class CanonicalHostNamePropertyDefiner extends PropertyDefinerBase {

    @Override
    public String getPropertyValue() {
        return new NetworkAddressUtil(getContext()).safelyGetCanonicalLocalHostName();
    }

}
