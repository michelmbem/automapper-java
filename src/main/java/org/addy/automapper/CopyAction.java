package org.addy.automapper;

public class CopyAction implements MappingAction {

	public static void copyValue(Object s, Property sp, Object d, Property dp, MappingContext c) {
		Object value = sp.getValue(s);
		
		if (TypeHelper.isAssignable(sp.getType(), dp.getType())) {
			dp.setValue(d, value);
		} else if (dp.getType().isPrimitive()) {
			try {
				dp.setValue(d, TypeHelper.convertTo(dp.getType(), value));
			} catch (IllegalArgumentException ignored) {
			}
		} else if (c != null && c.canMap(sp.getType(), dp.getType())) {
			dp.setValue(d, c.doMap(value, dp.getType()));
		}
	}

	@Override
	public void execute(Object src, Property srcProp, Object dest, Property destProp, MappingContext ctx) {
		copyValue(src, srcProp, dest, destProp, ctx);
	}

}
