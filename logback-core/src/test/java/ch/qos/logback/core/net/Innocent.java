package ch.qos.logback.core.net;

import java.util.Objects;

public class Innocent implements java.io.Serializable {

	private static final long serialVersionUID = -1227008349289885025L;

	int anInt;
	Integer anInteger;
	String aString;

	public int getAnInt() {
		return anInt;
	}

	public void setAnInt(final int anInt) {
		this.anInt = anInt;
	}

	public Integer getAnInteger() {
		return anInteger;
	}

	public void setAnInteger(final Integer anInteger) {
		this.anInteger = anInteger;
	}

	public String getaString() {
		return aString;
	}

	public void setaString(final String aString) {
		this.aString = aString;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (aString == null ? 0 : aString.hashCode());
		result = prime * result + anInt;
		return prime * result + (anInteger == null ? 0 : anInteger.hashCode());
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (getClass() != obj.getClass())) {
			return false;
		}
		final Innocent other = (Innocent) obj;
		if (!Objects.equals(aString, other.aString)) {
			return false;
		}
		if (anInt != other.anInt) {
			return false;
		}
		if (!Objects.equals(anInteger, other.anInteger)) {
			return false;
		}
		return true;
	}

}
