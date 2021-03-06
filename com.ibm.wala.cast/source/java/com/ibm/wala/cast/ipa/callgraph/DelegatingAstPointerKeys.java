/******************************************************************************
 * Copyright (c) 2002 - 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *****************************************************************************/
package com.ibm.wala.cast.ipa.callgraph;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.ibm.wala.classLoader.IField;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.ConcreteTypeKey;
import com.ibm.wala.ipa.callgraph.propagation.ConstantKey;
import com.ibm.wala.ipa.callgraph.propagation.FilteredPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerKeyFactory;
import com.ibm.wala.util.collections.NonNullSingletonIterator;
import com.ibm.wala.util.strings.Atom;

public class DelegatingAstPointerKeys implements AstPointerKeyFactory {
  private final PointerKeyFactory base;

  public DelegatingAstPointerKeys(PointerKeyFactory base) {
    this.base = base;
  }

  public PointerKey getPointerKeyForLocal(CGNode node, int valueNumber) {
    return base.getPointerKeyForLocal(node, valueNumber);
  }

  public FilteredPointerKey getFilteredPointerKeyForLocal(CGNode node, int valueNumber, FilteredPointerKey.TypeFilter filter) {
    return base.getFilteredPointerKeyForLocal(node, valueNumber, filter);
  }

  public PointerKey getPointerKeyForReturnValue(CGNode node) {
    return base.getPointerKeyForReturnValue(node);
  }

  public PointerKey getPointerKeyForExceptionalReturnValue(CGNode node) {
    return base.getPointerKeyForExceptionalReturnValue(node);
  }

  public PointerKey getPointerKeyForStaticField(IField f) {
    return base.getPointerKeyForStaticField(f);
  }

  public PointerKey getPointerKeyForObjectCatalog(InstanceKey I) {
    return new ObjectPropertyCatalogKey(I);
  }

  public PointerKey getPointerKeyForInstanceField(InstanceKey I, IField f) {
    return base.getPointerKeyForInstanceField(I, f);
  }

  public PointerKey getPointerKeyForArrayContents(InstanceKey I) {
    return base.getPointerKeyForArrayContents(I);
  }

  public Iterator<PointerKey> getPointerKeysForReflectedFieldRead(InstanceKey I, InstanceKey F) {
    List<PointerKey> result = new LinkedList<PointerKey>();

    if (F instanceof ConstantKey) {
      Object v = ((ConstantKey) F).getValue();
      if (v instanceof String) {
        IField f = I.getConcreteType().getField(Atom.findOrCreateUnicodeAtom((String) v));
        result.add(getPointerKeyForInstanceField(I, f));
      }
    }

    result.add(ReflectedFieldPointerKey.mapped(new ConcreteTypeKey(F.getConcreteType()), I));

    return result.iterator();
  }

  public Iterator<PointerKey> getPointerKeysForReflectedFieldWrite(InstanceKey I, InstanceKey F) {
    // FIXME: current only constant string are handled
    if (F instanceof ConstantKey) {
      Object v = ((ConstantKey) F).getValue();
      if (v instanceof String) {
        IField f = I.getConcreteType().getField(Atom.findOrCreateUnicodeAtom((String) v));
        return new NonNullSingletonIterator<PointerKey>(getPointerKeyForInstanceField(I, f));
      }
    }

    return new NonNullSingletonIterator<PointerKey>(ReflectedFieldPointerKey.mapped(new ConcreteTypeKey(F.getConcreteType()), I));
  }
}
