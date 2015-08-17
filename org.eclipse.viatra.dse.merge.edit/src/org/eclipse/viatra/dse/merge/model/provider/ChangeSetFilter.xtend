package org.eclipse.viatra.dse.merge.model.provider

import java.util.Collection
import org.eclipse.viatra.dse.merge.model.Create
import com.google.common.collect.Lists
import org.eclipse.viatra.dse.merge.model.Feature
import java.util.List

class ChangeSetFilter {
	
	def static filterChildren(Collection<Object> collection) {
		
		val features = Lists.<Feature>newArrayList
		collection.filter[x | x instanceof Create].forEach[ y | addFeaturesToList(y as Create, features)]
		return collection.filter[x | !features.contains(x)].toList
	}
	
	private def static addFeaturesToList(Create create, List<Feature> list) {
		list.addAll(create.attributes.toList)
		list.addAll(create.features.toList)
	}
	
}