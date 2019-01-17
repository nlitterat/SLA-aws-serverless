package com.nlitterat.sla;

public class ServiceMetric {
	private String dimensionName;
	private String dimensionValue;
	private String name;
	private int totalDataPoint;
	private int sum;
	/**
	 * @return the dimensionName
	 */
	public String getDimensionName() {
		return dimensionName;
	}
	/**
	 * @param dimensionName the dimensionName to set
	 */
	public void setDimensionName(String dimensionName) {
		this.dimensionName = dimensionName;
	}
	/**
	 * @return the dimensionValue
	 */
	public String getDimensionValue() {
		return dimensionValue;
	}
	/**
	 * @param dimensionValue the dimensionValue to set
	 */
	public void setDimensionValue(String dimensionValue) {
		this.dimensionValue = dimensionValue;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the totalDataPoint
	 */
	public int getTotalDataPoint() {
		return totalDataPoint;
	}
	/**
	 * @param totalDataPoint the totalDataPoint to set
	 */
	public void setTotalDataPoint(int totalDataPoint) {
		this.totalDataPoint = totalDataPoint;
	}
	/**
	 * @return the sum
	 */
	public int getSum() {
		return sum;
	}
	/**
	 * @param sum the sum to set
	 */
	public void setSum(int sum) {
		this.sum = sum;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ServiceMetric [dimensionName=" + dimensionName + ", dimensionValue=" + dimensionValue + ", name=" + name
				+ ", totalDataPoint=" + totalDataPoint + ", sum=" + sum + "]";
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dimensionValue == null) ? 0 : dimensionValue.hashCode());
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ServiceMetric other = (ServiceMetric) obj;
		if (dimensionValue == null) {
			if (other.dimensionValue != null)
				return false;
		} else if (!dimensionValue.equals(other.dimensionValue))
			return false;
		return true;
	}
	


}
