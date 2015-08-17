package org.eclipse.viatra.dse.merge.model.provider;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.emf.common.util.EList;
import org.eclipse.viatra.dse.merge.model.Attribute;
import org.eclipse.viatra.dse.merge.model.Create;
import org.eclipse.viatra.dse.merge.model.Feature;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

@SuppressWarnings("all")
public class ChangeSetFilter {
  public static List<Object> filterChildren(final Collection<Object> collection) {
    final ArrayList<Feature> features = Lists.<Feature>newArrayList();
    final Function1<Object, Boolean> _function = new Function1<Object, Boolean>() {
      @Override
      public Boolean apply(final Object x) {
        return Boolean.valueOf((x instanceof Create));
      }
    };
    Iterable<Object> _filter = IterableExtensions.<Object>filter(collection, _function);
    final Procedure1<Object> _function_1 = new Procedure1<Object>() {
      @Override
      public void apply(final Object y) {
        ChangeSetFilter.addFeaturesToList(((Create) y), features);
      }
    };
    IterableExtensions.<Object>forEach(_filter, _function_1);
    final Function1<Object, Boolean> _function_2 = new Function1<Object, Boolean>() {
      @Override
      public Boolean apply(final Object x) {
        boolean _contains = features.contains(x);
        return Boolean.valueOf((!_contains));
      }
    };
    Iterable<Object> _filter_1 = IterableExtensions.<Object>filter(collection, _function_2);
    return IterableExtensions.<Object>toList(_filter_1);
  }
  
  private static boolean addFeaturesToList(final Create create, final List<Feature> list) {
    boolean _xblockexpression = false;
    {
      EList<Attribute> _attributes = create.getAttributes();
      List<Attribute> _list = IterableExtensions.<Attribute>toList(_attributes);
      list.addAll(_list);
      EList<Feature> _features = create.getFeatures();
      List<Feature> _list_1 = IterableExtensions.<Feature>toList(_features);
      _xblockexpression = list.addAll(_list_1);
    }
    return _xblockexpression;
  }
}
