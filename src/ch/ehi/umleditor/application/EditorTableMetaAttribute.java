package ch.ehi.umleditor.application;

import javax.swing.table.*;

public class EditorTableMetaAttribute {
	private DefaultTableModel ivjDtmMetaAttributes = null;
	
	public EditorTableMetaAttribute() {
		if (ivjDtmMetaAttributes == null) {
			ivjDtmMetaAttributes = new DefaultTableModel();
			}
		}
	public DefaultTableModel getTableModel() {
		return ivjDtmMetaAttributes;
		}
	public void addCol (Object object, Object object2) {
		ivjDtmMetaAttributes.addColumn(object);
		ivjDtmMetaAttributes.addColumn(object2);
	}
	public void addRow (String Name, String Value) {
		ivjDtmMetaAttributes.addRow(new Object[]{Name,Value});
	}
}
