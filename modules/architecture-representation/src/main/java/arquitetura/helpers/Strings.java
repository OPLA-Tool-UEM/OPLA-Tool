package arquitetura.helpers;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Joiner;

import arquitetura.representation.Variability;
import arquitetura.representation.Variant;

public class Strings {
	
	public static String spliterVariants(List<Variant> list) {
		List<String> names = new ArrayList<String>();
		
		for (Variant variant : list)
			names.add(variant.getName());
		
		return Joiner.on(", ").skipNulls().join(names);
	}
	
	public static String spliterVariabilities(List<Variability> list) {
		List<String> names = new ArrayList<String>();
		
		for(Variability variability : list)
			names.add(variability.getName());
		
		return Joiner.on(", ").skipNulls().join(names);
	}

}
