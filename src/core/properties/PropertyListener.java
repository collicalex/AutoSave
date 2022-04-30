package core.properties;

public interface PropertyListener {
	
	public void propertyUpdateSrc(Property property, String newValue);
	public void propertyUpdateDst(Property property, String newValue);
	public void propertyUpdateRecur(Property property, boolean newValue);
	public void propertyUpdateAddIgnore(Property property, String newValue);
	public void propertyUpdateRemoveIgnore(Property property, String newValue);
	public void propertyUpdateIcon(Property property, String newValue);
	
}
