package ch.ehi.umleditor.application;

import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import ch.ehi.interlis.associations.AssociationDef;
import ch.ehi.interlis.associations.RoleDef;
import ch.ehi.interlis.attributes.AttributeDef;
import ch.ehi.interlis.attributes.AttributeValueUsage;
import ch.ehi.interlis.attributes.DomainAttribute;
import ch.ehi.interlis.constraints.ConstraintDef;
import ch.ehi.interlis.domainsandconstants.DomainDef;
import ch.ehi.interlis.domainsandconstants.Type;
import ch.ehi.interlis.domainsandconstants.linetypes.LineFormTypeDef;
import ch.ehi.interlis.functions.FunctionDef;
import ch.ehi.interlis.graphicdescriptions.GraphicDef;
import ch.ehi.interlis.graphicdescriptions.GraphicParameterDef;
import ch.ehi.interlis.metaobjects.MetaDataUseDef;
import ch.ehi.interlis.metaobjects.ParameterDef;
import ch.ehi.interlis.modeltopicclass.ClassDef;
import ch.ehi.interlis.modeltopicclass.INTERLIS2Def;
import ch.ehi.interlis.modeltopicclass.IliImport;
import ch.ehi.interlis.modeltopicclass.ModelDef;
import ch.ehi.interlis.modeltopicclass.TopicDef;
import ch.ehi.interlis.modeltopicclass.TopicDepends;
import ch.ehi.interlis.tools.AbstractClassDefUtility;
import ch.ehi.interlis.tools.ModelElementUtility;
import ch.ehi.interlis.units.UnitDef;
import ch.ehi.interlis.views.ViewDef;
import ch.ehi.interlis.views.ViewableDef;
import ch.ehi.uml1_4.foundation.core.Feature;
import ch.ehi.uml1_4.foundation.core.ModelElement;
import ch.ehi.uml1_4.implementation.AbstractModelElement;
import ch.ehi.umleditor.interlis.iliexport.TransferFromUmlMetamodel;

/**
 * This class compare two interlis models and update a given model
 * @author alelizzt
 *
 */
public class CompareInterlis2Def {
	/**
	 * Resources from class 'CompareInterlis2Def'
	 */
	static ResourceBundle rsrc = ch.ehi.basics.i18n.ResourceBundle.getBundle(CompareInterlis2Def.class);

	/**
	 * INTERLIS model to update
	 */
	public INTERLIS2Def oldInterlis;
	/**
	 * INTERLIS model to check update
	 */
	public INTERLIS2Def newInterlis;

	TransferFromUmlMetamodel obj = new TransferFromUmlMetamodel();
	
	/**
	 * Get the INTERLIS model value to update
	 * @return model to update
	 */
	public INTERLIS2Def getOldInterlis() {
		return oldInterlis;
	}

	/**
	 * Set the INTERLIS model value to update
	 * @param oldInterlis
	 */
	public void setOldInterlis(INTERLIS2Def oldInterlis) {
		this.oldInterlis = oldInterlis;
	}

	/**
	 * Get the INTERLIS model to check update
	 * @return model to check update
	 */
	public INTERLIS2Def getNewInterlis() {
		return newInterlis;
	}

	/**
	 * Set the INTERLIS model value to check update
	 * @param newInterlis
	 */
	public void setNewInterlis(INTERLIS2Def newInterlis) {
		this.newInterlis = newInterlis;
	}

	/**
	 * Compare INTERLIS models to update
	 */
	public void compareINTERLIS() {
		// enumerate and sort all ModelDef
		TransferFromUmlMetamodel obj = new TransferFromUmlMetamodel();

		// Update attributes of InterlisDef
		updateINTERLIS2Def(oldInterlis, newInterlis);
		
		// List with sorted models from InterlisFile
		List listold = obj.sortIliDefs(ModelElementUtility.getChildElements(oldInterlis, ModelDef.class));
		List listnew = obj.sortIliDefs(ModelElementUtility.getChildElements(newInterlis, ModelDef.class));
		
		// recorrer interlisDef nuevo si hay modelos nuevos, se copian en el interlisDef viejo
		addNewModels(oldInterlis, listold, listnew);
		
		removeAndUpdateOldModels(newInterlis, listold, listnew);
		
		// visit all ModelDef
		for (int i = 0; i < listold.size(); i++) {
			ModelDef modold = (ModelDef) listold.get(i);
			String oldName = modold.getName().getValue();		
			int newIndex = findIndexInListByName(oldName, listnew);
			if (newIndex == -1) {
				LauncherView.getInstance().log(rsrc.getString("CIFuncDesc"), oldName+" Deleted");
			} else {
				ModelDef modnew = (ModelDef) listold.get(newIndex);
				LauncherView.getInstance().log(rsrc.getString("CIFuncDesc"),"Updating "+ modold.getName().getValue()+" ...");
				Iterator<?> it = modnew.iteratorOwnedElement();
			    while(it.hasNext()) {
			    	Object ownedEle = it.next();
			    	if(ownedEle instanceof ch.ehi.interlis.modeltopicclass.TopicDef) {
			    		TopicDef newTopic = (TopicDef) ownedEle;
			    		String topicName = newTopic.getName().getValue();
			    		TopicDef oldTopic = (TopicDef)findOwnedElementByName(topicName, modold);
			    		if(oldTopic != null) {
				    		LauncherView.getInstance().log(rsrc.getString("CIFuncDesc"),"Updating "+ oldTopic.getName().getValue()+" ...");
				    		updateTopicDef(oldTopic, newTopic); 
			    		}
			    		else {
			    			LauncherView.getInstance().log(rsrc.getString("CIFuncDesc"),"Creating "+ newTopic.getName().getValue()+" ...");
			    			newInterlis.addOwnedElement(newTopic);
			    		}
			    		
			    	} else if(ownedEle instanceof ch.ehi.interlis.domainsandconstants.DomainDef){
			    		DomainDef newDomain = (DomainDef) ownedEle;
			    		String domainName = newDomain.getName().getValue();
			    		DomainDef oldDomain = (DomainDef)findOwnedElementByName(domainName, modold);
			    		LauncherView.getInstance().log(rsrc.getString("CIFuncDesc"),"Updating "+ oldDomain.getName().getValue()+" ...");
			    		updateDomainDef(oldDomain, newDomain);
			    	} else if(ownedEle instanceof ClassDef){
			    		ClassDef newClass = (ClassDef) ownedEle;
			    		String className = newClass.getName().getValue();
			    		ClassDef oldClass = (ClassDef)findOwnedElementByName(className, modold);
			    		LauncherView.getInstance().log(rsrc.getString("CIFuncDesc"),"Updating "+ oldClass.getName().getValue()+" ...");
			    		updateClassDef(oldClass, newClass);
			    	} else if(ownedEle instanceof GraphicParameterDef){
			    		GraphicParameterDef newGraphic = (GraphicParameterDef) ownedEle;
			    		String genName = newGraphic.getName().getValue();
			    		GraphicParameterDef oldGraphic = (GraphicParameterDef)findOwnedElementByName(genName, modold);
			    		LauncherView.getInstance().log(rsrc.getString("CIFuncDesc"),"Updating "+ oldGraphic.getName().getValue()+" ...");
			    		updateGraphicParameterDef(oldGraphic, newGraphic);
			    	} else if(ownedEle instanceof MetaDataUseDef){
			    		MetaDataUseDef newMetadata = (MetaDataUseDef) ownedEle;
			    		String metadataName = newMetadata.getName().getValue();
			    		MetaDataUseDef oldMetadata = (MetaDataUseDef)findOwnedElementByName(metadataName, modold);
			    		LauncherView.getInstance().log(rsrc.getString("CIFuncDesc"),"Updating "+ oldMetadata.getName().getValue()+" ...");
			    		updateMetadataUseDef(oldMetadata, newMetadata);
			    	}  else if(ownedEle instanceof FunctionDef){
			    		FunctionDef newFunction = (FunctionDef) ownedEle;
			    		String functionName = newFunction.getName().getValue();
			    		FunctionDef oldFunction = (FunctionDef)findOwnedElementByName(functionName, modold);
			    		LauncherView.getInstance().log(rsrc.getString("CIFuncDesc"),"Updating "+ oldFunction.getName().getValue()+" ...");
			    		updateFunctionDef(oldFunction, newFunction);
			    	} else {
			    		System.out.println(ownedEle.getClass());
			    	}
			    }
			    System.out.println("bye");
			    LauncherView.getInstance().log(rsrc.getString("CIFuncDesc"), "Old "+newInterlis.getName().getValue()+" Deleted!");
			    ElementFactory.removeElement(newInterlis);
			   
			}			
		}
		LauncherView.getInstance().log(rsrc.getString("CIFuncDesc"), oldInterlis.getName().getValue()+" Updated!");
	}
	
	private void updateMetadataUseDef(MetaDataUseDef oldMetadata, MetaDataUseDef newMetadata) {
		oldMetadata.setBasketOid(newMetadata.getBasketOid());
		oldMetadata.setKind(newMetadata.getKind());
		oldMetadata.setDocumentation(newMetadata.getDocumentation());
		//
		oldMetadata.setSyntax(newMetadata.getSyntax());
		
	}

	private void updateGraphicParameterDef(GraphicParameterDef oldGraphic, GraphicParameterDef newGraphic) {
		oldGraphic.setDocumentation(newGraphic.getDocumentation());
		//depends on missed
		oldGraphic.setSyntax(newGraphic.getSyntax());
		Iterator old = oldGraphic.iteratorClientDependency();
		Iterator newold = newGraphic.iteratorClientDependency();
		
	}

	private void updateFunctionDef(FunctionDef oldFunction, FunctionDef newFunction) {
		oldFunction.setDocumentation(newFunction.getDocumentation());
		//depends on missed
		oldFunction.setSyntax(newFunction.getSyntax());
		
	}

	private void updateTopicDef(TopicDef oldTopic, TopicDef newTopic) {
		oldTopic.setKind(newTopic.getKind());
		oldTopic.setDocumentation(newTopic.getDocumentation());
		oldTopic.setAbstract(newTopic.isAbstract());
		oldTopic.setPropFinal(newTopic.isPropFinal());
		updateExtendsOfTopicDef(oldTopic,newTopic);
		if(newTopic.containsOiddomain()) {
			oldTopic.attachOiddomain(newTopic.detachOiddomain());
		}
		// Dependency
		updateDependencyOfTopicDef(oldTopic,newTopic);
		List oldTopicChildren = obj.sortIliDefs(ch.ehi.interlis.tools.ModelElementUtility.getChildElements(oldTopic, null));
		List newTopicChildren = obj.sortIliDefs(ch.ehi.interlis.tools.ModelElementUtility.getChildElements(newTopic, null));
		// actualizando hijos :P
		addNewChildTopics(oldTopic, oldTopicChildren, newTopicChildren);
		removeAndUpdateOldChildTopic(oldTopic, oldTopicChildren, newTopicChildren);
	}

	private void updateDependencyOfTopicDef(TopicDef oldTopic, TopicDef newTopic) {
		Iterator dependsi = oldTopic.iteratorClientDependency();
		int i = 0;
		while (dependsi.hasNext()) {
			Object obj = dependsi.next();
			if (obj instanceof ch.ehi.interlis.modeltopicclass.TopicDepends) {
				ch.ehi.interlis.modeltopicclass.TopicDepends depends = (ch.ehi.interlis.modeltopicclass.TopicDepends) obj;
				Iterator supplieri = depends.iteratorSupplier();
				if (supplieri.hasNext()) {
					TopicDef supplier = (TopicDef) supplieri.next();
					String oldName = supplier.getName().getValue();
					ch.ehi.interlis.modeltopicclass.TopicDepends newdepend = findDependByName(oldName, newTopic);
					if(newdepend == null) {
						oldTopic.removeClientDependency(depends);
					}
					
				}
			} else {
				// ignore other kind of Dependency
			}
		}
		
		Iterator newdependsi = newTopic.iteratorClientDependency();//clientdependency?
		while (newdependsi.hasNext()) {
			Object obj = newdependsi.next();
			if (obj instanceof ch.ehi.interlis.modeltopicclass.TopicDepends) {
				ch.ehi.interlis.modeltopicclass.TopicDepends newdepend = (ch.ehi.interlis.modeltopicclass.TopicDepends) obj;
				ModelElement supplier = (ModelElement)newdepend.iteratorSupplier().next();
				String newName = supplier.getName().getValue();
				ch.ehi.interlis.modeltopicclass.TopicDepends oldimport = findDependByName(newName, oldTopic);
				if (oldimport == null) {
					// si no existe lo agrega
					oldTopic.addClientDependency(newdepend);
				} else {
					// si existe
				}
			} else {
				//nothing to do
			}
			
		}
	}

	private TopicDepends findDependByName(String findname, TopicDef newTopic) {
		Iterator it = newTopic.iteratorClientDependency();
		while (it.hasNext()) {
			Object obj = it.next();
			if (obj instanceof ch.ehi.interlis.modeltopicclass.TopicDepends) {				
				ch.ehi.interlis.modeltopicclass.TopicDepends depends = (ch.ehi.interlis.modeltopicclass.TopicDepends) obj;
				
				TopicDef supplier = (TopicDef)depends.iteratorSupplier().next();
				String name = supplier.getName().getValue();				
				if (name.equals(findname)) {
					return depends;
				}
			} else {
				// nothing to do 
 			}
 		}
		return null;
	}

	private void updateExtendsOfTopicDef(TopicDef oldTopic, TopicDef newTopic) {
		Iterator extendsi = newTopic.iteratorGeneralization();
		while (extendsi.hasNext()) {
			Object obj = extendsi.next();
			if (obj instanceof ch.ehi.interlis.modeltopicclass.TopicExtends) {
				ch.ehi.interlis.modeltopicclass.TopicExtends extend = (ch.ehi.interlis.modeltopicclass.TopicExtends) obj;
				if (extend.containsParent()) {
					TopicDef supplier = (TopicDef) extend.getParent();
					System.out.println(newTopic.getName().getValue()+" hereda de "+supplier.getName().getValue());
					oldTopic.clearGeneralization();
					oldTopic.addGeneralization(extend);
				}
			}
		}
	}

	private void addNewChildTopics(TopicDef oldTopic, List oldTopicChildren, List newTopicChildren) {
		for (int i = 0; i < newTopicChildren.size(); i++) {
			ModelElement modElementNew = (ModelElement) newTopicChildren.get(i);
			String newName = modElementNew.getName().getValue();		
			int oldIndex = findIndexInListByName(newName, oldTopicChildren);
			if (oldIndex == -1) { // si no esta en la lista vieja se agrega
				LauncherView.getInstance().log(rsrc.getString("CIFuncDesc"),"Creating "+newName+" ...");
				if (modElementNew instanceof DomainDef) {
					oldTopic.addOwnedElement(modElementNew);
				}
				else if(modElementNew instanceof ClassDef) {
					oldTopic.addOwnedElement(modElementNew);
				}
				else if(modElementNew instanceof FunctionDef) {
					oldTopic.addOwnedElement(modElementNew);
				}
				else if(modElementNew instanceof GraphicDef) {
					oldTopic.addOwnedElement(modElementNew);
				}
				else if(modElementNew instanceof MetaDataUseDef) {
					oldTopic.addOwnedElement(modElementNew);
				}
				else if(modElementNew instanceof AssociationDef) {
					oldTopic.addOwnedElement(modElementNew);
				}
				else if(modElementNew instanceof UnitDef) {
					oldTopic.addOwnedElement(modElementNew);
				}
				else if(modElementNew instanceof ViewDef) {
					oldTopic.addOwnedElement(modElementNew);
				}
				else {
					System.out.println("Clases por agregar:");
					System.out.println(modElementNew.getClass());
				}
				
			} else {
				
			}
		}
	}

	public int findIndexInListByName(String oldName, List listnew) {
		for (int i = 0; i < listnew.size(); i++) {
			String newName = "";
			if (listnew.get(i) instanceof ModelDef) {
				ModelDef modnew = (ModelDef) listnew.get(i);
				newName = modnew.getName().getValue();		
			}
			else if(listnew.get(i) instanceof DomainDef) {
				DomainDef domnew = (DomainDef) listnew.get(i);
				newName = domnew.getName().getValue();
			}
			else if(listnew.get(i) instanceof TopicDef) {
				TopicDef topicnew = (TopicDef) listnew.get(i);
				newName = topicnew.getName().getValue();
			}
			else if(listnew.get(i) instanceof ClassDef) {
				ClassDef classnew = (ClassDef) listnew.get(i);
				newName = classnew.getName().getValue();
			}
			else if(listnew.get(i) instanceof UnitDef) {
				UnitDef unitnew = (UnitDef) listnew.get(i);
				newName = unitnew.getName().getValue();
			}
			else if(listnew.get(i) instanceof ViewDef) {
				ViewDef unitnew = (ViewDef) listnew.get(i);
				newName = unitnew.getName().getValue();
			}
			else if(listnew.get(i) instanceof AssociationDef) {
				AssociationDef asonew = (AssociationDef) listnew.get(i);
				newName = asonew.getName().getValue();
			}
			else if(listnew.get(i) instanceof GraphicDef) {
				GraphicDef asonew = (GraphicDef) listnew.get(i);
				newName = asonew.getName().getValue();
			}
			else if(listnew.get(i) instanceof MetaDataUseDef) {
				MetaDataUseDef asonew = (MetaDataUseDef) listnew.get(i);
				newName = asonew.getName().getValue();
			}
			else if(listnew.get(i) instanceof LineFormTypeDef) {
				LineFormTypeDef asonew = (LineFormTypeDef) listnew.get(i);
				newName = asonew.getName().getValue();
			}
			else if(listnew.get(i) instanceof AttributeDef) {
				AttributeDef asonew = (AttributeDef) listnew.get(i);
				newName = asonew.getName().getValue(); 
			}
			else if(listnew.get(i) instanceof RoleDef) {
				RoleDef asonew = (RoleDef) listnew.get(i);
				newName = asonew.getName().getValue(); 
			}
			// compara los nombres encontrados
			if (newName.equals(oldName)) {
				return i;
			}
			
		}
		return -1;
	}
	
	public Object findOwnedElementByName(String findname, ModelDef model) {
		Iterator<?> it = model.iteratorOwnedElement();
	    while(it.hasNext()) {
	    	Object ownedEle = it.next();
	    	System.out.println(ownedEle);
	    	String name = "";
	    	if(ownedEle instanceof ch.ehi.interlis.modeltopicclass.TopicDef) {
	    		TopicDef topic = (TopicDef) ownedEle;
	    		name = topic.getName().getValue();
	    	} else if(ownedEle instanceof ch.ehi.interlis.domainsandconstants.DomainDef){
	    		DomainDef domain = (DomainDef) ownedEle;
	    		name = domain.getName().getValue();
	    	} else if(ownedEle instanceof ClassDef){
	    		ClassDef classd = (ClassDef) ownedEle;
	    		name = classd.getName().getValue();
	    	} else if(ownedEle instanceof GraphicParameterDef){
	    		GraphicParameterDef graphic = (GraphicParameterDef) ownedEle;
	    		name = graphic.getName().getValue();
	    	} else if(ownedEle instanceof MetaDataUseDef){
	    		MetaDataUseDef metadata = (MetaDataUseDef) ownedEle;
	    		name = metadata.getName().getValue();
	    	} else if(ownedEle instanceof FunctionDef){
	    		FunctionDef function = (FunctionDef) ownedEle;
	    		name = function.getName().getValue();
	    	}
	    	if (name.equals(findname)) {
	    		return ownedEle;
	    	}
	    }
		return null;
	}
	
	public void addNewModels(INTERLIS2Def oldInterlis, List listold, List listnew) {
		
		for (int i = 0; i < listnew.size(); i++) {
			ModelDef modnew = (ModelDef) listnew.get(i);
			String newName = modnew.getName().getValue();		
			int oldIndex = findIndexInListByName(newName, listold);
			if (oldIndex == -1) {
				LauncherView.getInstance().log(rsrc.getString("CIFuncDesc"),"Creating "+newName+" ...");
				if (modnew instanceof ModelDef) {
					oldInterlis.addOwnedElement(modnew);
				} 
				
			} else {
			
			}
		}
	}
	
	public void updateINTERLIS2Def(INTERLIS2Def oldInterlis, INTERLIS2Def newInterlis) {
		oldInterlis.setVersion(newInterlis.getVersion());
		oldInterlis.setDocumentation(newInterlis.getDocumentation());
	}
	
	public void removeAndUpdateOldModels(INTERLIS2Def newInterlis, List listold, List listnew) {
		
		for (int i = 0; i < listold.size(); i++) {
			ModelDef modold = (ModelDef) listold.get(i);
			String oldName = modold.getName().getValue();		
			int newIndex = findIndexInListByName(oldName, listnew);
			if (newIndex == -1) {
				LauncherView.getInstance().log(rsrc.getString("CIFuncDesc"),"Deleting "+ oldName+" ...");
				if (modold instanceof ModelDef) {
					oldInterlis.removeOwnedElement(modold);
				} else {
					System.out.println("Agrega un caso para eliminar la clase no registrada:");
					System.out.println(modold.getClass());
				}
				
			} else {
				// como se que ya existe el modelo en ambas listas, voy a actualizar
				ModelDef modnew = (ModelDef) listnew.get(newIndex);
				updateModelDef(modold, modnew);
			}
		}
	}

	

	public void removeAndUpdateOldChildTopic(TopicDef oldTopic, List oldTopicChildren, List newTopicChildren) {

		for (int i = 0; i < oldTopicChildren.size(); i++) {
			ModelElement oldTopicElement = (ModelElement) oldTopicChildren.get(i);
			String oldName = oldTopicElement.getName().getValue();
			int newIndex = findTopicElementIndexInListByName(oldName, newTopicChildren); //search old element in new list
			if (newIndex == -1) { // elimina viejo elemento porque no la encuentra en la nueva lista
				LauncherView.getInstance().log(rsrc.getString("CIFuncDesc"),"Deleting "+oldName+" ...");
				if (oldTopicElement instanceof ClassDef) {
					oldTopic.removeOwnedElement(oldTopicElement);
				} 
				else if(oldTopicElement instanceof DomainDef) {
					oldTopic.removeOwnedElement(oldTopicElement);					
				}
				else if(oldTopicElement instanceof FunctionDef) {
					oldTopic.removeOwnedElement(oldTopicElement);					
				}
				else if(oldTopicElement instanceof GraphicDef) {
					oldTopic.removeOwnedElement(oldTopicElement);
				}
				else if(oldTopicElement instanceof MetaDataUseDef) {
					oldTopic.removeOwnedElement(oldTopicElement);
				}
				else if(oldTopicElement instanceof TopicDef) {
					oldTopic.removeOwnedElement(oldTopicElement);
				}
				else if(oldTopicElement instanceof AssociationDef) {
					oldTopic.removeOwnedElement(oldTopicElement);
				}
				else if(oldTopicElement instanceof UnitDef) {
					oldTopic.removeOwnedElement(oldTopicElement);
				}
				else if(oldTopicElement instanceof ViewDef) {
					oldTopic.removeOwnedElement(oldTopicElement);
				}
				else {
					System.out.println("Agrega un caso para eliminar la clase no registrada:");
					System.out.println(oldTopic.getClass());
				}
				
			}
			else {// como se que ya existe el topic en ambas listas, voy a actualizar
				
				if (oldTopicElement instanceof ClassDef) {
					ClassDef oldClass = (ClassDef) oldTopicElement;
					ClassDef newClass = (ClassDef) newTopicChildren.get(newIndex);
					updateClassDef(oldClass, newClass);
				}
				else if (oldTopicElement instanceof DomainDef) {
					DomainDef newDom = (DomainDef) newTopicChildren.get(newIndex);
					DomainDef oldDom = (DomainDef) oldTopicElement;
					LauncherView.getInstance().log(rsrc.getString("CIFuncDesc"),"Updating "+ oldDom.getName().getValue()+" ...");
					updateDomainDef(oldDom, newDom);
 
				} else if (oldTopicElement instanceof FunctionDef) {
					FunctionDef newFnc = (FunctionDef) newTopicChildren.get(newIndex);
					FunctionDef oldFnc = (FunctionDef) oldTopicElement;
					LauncherView.getInstance().log(rsrc.getString("CIFuncDesc"),"Updating "+ oldFnc.getName().getValue()+" ...");
					updateFunctionDef(oldFnc, newFnc);

				} else if (oldTopicElement instanceof GraphicDef) {
					GraphicDef graphNew = (GraphicDef) newTopicChildren.get(newIndex);
					GraphicDef graphOld = (GraphicDef) oldTopicElement;
					LauncherView.getInstance().log(rsrc.getString("CIFuncDesc"),"Updating "+graphOld.getName().getValue()+" ...");
					updateGraphicDef(graphOld, graphNew);
 
				} else if (oldTopicElement instanceof LineFormTypeDef ) {
					LineFormTypeDef newLft = (LineFormTypeDef) newTopicChildren.get(newIndex);
					LineFormTypeDef oldLft = (LineFormTypeDef) oldTopicElement;
					LauncherView.getInstance().log(rsrc.getString("CIFuncDesc"),"Updating "+ oldLft.getName().getValue()+" ...");
					updateLineFormTypeDef(oldLft, newLft); 

				} else if (oldTopicElement instanceof MetaDataUseDef) {
					MetaDataUseDef newMetadata = (MetaDataUseDef) newTopicChildren.get(newIndex);
					MetaDataUseDef oldMetadata = (MetaDataUseDef) oldTopicElement;
					LauncherView.getInstance().log(rsrc.getString("CIFuncDesc"),"Updating "+ oldMetadata.getName().getValue()+" ...");
					updateMetadataUseDef(oldMetadata, newMetadata);	

				} else if (oldTopicElement instanceof AssociationDef ) {
					AssociationDef newAssociation = (AssociationDef) newTopicChildren.get(newIndex);
					AssociationDef oldAssociation = (AssociationDef) oldTopicElement;
					LauncherView.getInstance().log(rsrc.getString("CIFuncDesc"),"Updating "+ oldAssociation.getName().getValue()+" ...");
					updateAssociationDef(oldAssociation, newAssociation); 

				} else if (oldTopicElement instanceof UnitDef) {
					UnitDef newUnit = (UnitDef) newTopicChildren.get(newIndex);
					UnitDef oldUnit = (UnitDef) oldTopicElement;
					LauncherView.getInstance().log(rsrc.getString("CIFuncDesc"),"Updating "+ oldUnit.getName().getValue()+" ...");
					updateUnitDef(oldUnit, newUnit);
					 
				}
			}
				}
	}
	
	private void updateLineFormTypeDef(LineFormTypeDef oldLft, LineFormTypeDef newLft) {
		oldLft.setDocumentation(newLft.getDocumentation());
		if(newLft.containsStructure()) {
			oldLft.attachStructure(newLft.detachStructure());
		}
		
	}

	private int findTopicElementIndexInListByName(String oldName, List newTopicChildren) {
		for (int i = 0; i < newTopicChildren.size(); i++) {
			ModelElement modnew = (ModelElement) newTopicChildren.get(i);
			String newName = modnew.getName().getValue();	
			if (newName.equals(oldName)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Update model definitions
	 * @param modold
	 * @param modnew
	 */
	public void updateModelDef(ModelDef modold, ModelDef modnew) {
		updateAtributesOfModelDef(modold, modnew);
		updateImportsOfModelDef(modold, modnew);
		//updateTranslationsOfModelDef(modold, modnew);
		 
		List newChildModel = obj.sortIliDefs(ModelElementUtility.getChildElements(modnew, null)); 
		List oldChildModel = obj.sortIliDefs(ModelElementUtility.getChildElements(modold, null)); 
		
		// Goes through new modelDef if there are new model children, they are copied into the old modelDef
		addNewChildModels(modold, oldChildModel, newChildModel);
		removeAndUpdateOldChildModelDef(modold, oldChildModel, newChildModel);
	}

	/**
	 * Delete and update old model definitions
	 * @param modold
	 * @param oldChildModel
	 * @param newChildModel
	 */
	private void removeAndUpdateOldChildModelDef(ModelDef modold, List oldChildModel, List newChildModel) {
		for (int i = 0; i < oldChildModel.size(); i++) {
			ModelElement modElementOld = (ModelElement) oldChildModel.get(i);
			String oldName = modElementOld.getName().getValue();		
			int newIndex = findIndexInListByName(oldName, newChildModel);
			if (newIndex == -1) {
				LauncherView.getInstance().log(rsrc.getString("CIFuncDesc"),"Deleting "+ oldName+" ...");
				if (modElementOld instanceof ClassDef) {
					modold.removeOwnedElement(modElementOld);
				} 
				else if(modElementOld instanceof DomainDef) {
					modold.removeOwnedElement(modElementOld);					
				}
				else if(modElementOld instanceof FunctionDef) {
					modold.removeOwnedElement(modElementOld);					
				}
				else if(modElementOld instanceof GraphicParameterDef) {
					modold.removeOwnedElement(modElementOld);
				}
				else if(modElementOld instanceof MetaDataUseDef) {
					modold.removeOwnedElement(modElementOld);
				}
				else if(modElementOld instanceof TopicDef) {
					modold.removeOwnedElement(modElementOld);
				}
				else {
					System.out.println("Agrega un caso para eliminar la clase no registrada:");
					System.out.println(modold.getClass());
				}
				
			} else {
				// As the element already exists in both models, I'm going to update
				if (modElementOld instanceof ClassDef) {
					ClassDef clsNew = (ClassDef) newChildModel.get(newIndex);
					ClassDef clsOld = (ClassDef) modElementOld;
					updateClassDef(clsOld, clsNew);
				} 
				else if(modElementOld instanceof DomainDef) {
					DomainDef newDomain = (DomainDef) newChildModel.get(newIndex);
					DomainDef oldDomain = (DomainDef) modElementOld;
					updateDomainDef(oldDomain, newDomain); 					
				}
				else if(modElementOld instanceof FunctionDef) {
					FunctionDef newFunction = (FunctionDef) newChildModel.get(newIndex);
					FunctionDef oldFunction = (FunctionDef) modElementOld;
					updateFunctionDef(oldFunction, newFunction);
				}
				else if(modElementOld instanceof GraphicParameterDef) {
					GraphicParameterDef newGraphic = (GraphicParameterDef) newChildModel.get(newIndex);
					GraphicParameterDef oldGraphic = (GraphicParameterDef) modElementOld;
					updateGraphicParameterDef(oldGraphic, newGraphic);
				}
				else if(modElementOld instanceof MetaDataUseDef) {
					MetaDataUseDef mtdNew = (MetaDataUseDef) newChildModel.get(newIndex);
					MetaDataUseDef mtdOld = (MetaDataUseDef) modElementOld;
					updateMetaDataUseDef(mtdOld, mtdNew);
				}
				else if(modElementOld instanceof TopicDef) {
					TopicDef newTopic = (TopicDef) newChildModel.get(newIndex);
					TopicDef oldTopic = (TopicDef) modElementOld;
					updateTopicDef(oldTopic, newTopic);
				}
				
			}
		}
		
	}

	/**
	 * Add new child (Topic, Domain, Class...) to old model
	 * @param modold
	 * @param oldChildModel
	 * @param newChildModel
	 */
	private void addNewChildModels(ModelDef modold, List oldChildModel, List newChildModel) {
		for (int i = 0; i < newChildModel.size(); i++) {
			ModelElement modElementNew = (ModelElement) newChildModel.get(i);
			String newName = modElementNew.getName().getValue();		
			int oldIndex = findIndexInListByName(newName, oldChildModel);
			if (oldIndex == -1) { // si no esta en la lista vieja se agrega
				LauncherView.getInstance().log(rsrc.getString("CIFuncDesc"),"Creating "+newName+" ...");
				if (modElementNew instanceof ModelDef) {
					oldInterlis.addOwnedElement(modElementNew);
				}
				else if(modElementNew instanceof DomainDef) {
					modold.addOwnedElement(modElementNew);
				}
				else if(modElementNew instanceof ClassDef) {
					modold.addOwnedElement(modElementNew);
				}
				else if(modElementNew instanceof FunctionDef) {
					modold.addOwnedElement(modElementNew);
				}
				else if(modElementNew instanceof ch.interlis.ili2c.metamodel.GraphicParameterDef) {
					modold.addOwnedElement(modElementNew);
				}
				else if(modElementNew instanceof MetaDataUseDef) {
					modold.addOwnedElement(modElementNew);
				}
				else if(modElementNew instanceof LineFormTypeDef) {
					modold.addOwnedElement(modElementNew);
				}
				else if(modElementNew instanceof TopicDef) {
					modold.addOwnedElement(modElementNew);
				}
				else if(modElementNew instanceof UnitDef) {
					modold.addOwnedElement(modElementNew);
				}
				else {
					System.out.println("Clases por agregar:");
					System.out.println(modElementNew.getClass());
				}
				
			} else {
				
			}
		}
		
	}

	/**
	 * Add imports to old model definitions
	 * @param modold
	 * @param modnew
	 */
	private void updateImportsOfModelDef(ModelDef modold, ModelDef modnew) {
		Iterator oldimportsi = modold.iteratorIliImport();
		while (oldimportsi.hasNext()) {
			Object obj = oldimportsi.next();
			ch.ehi.interlis.modeltopicclass.IliImport oldimport = (ch.ehi.interlis.modeltopicclass.IliImport) obj;
			if(oldimport.sizeSupplier() != 0) {
				ModelElement supplier = (ModelElement)oldimport.iteratorSupplier().next();
				String oldName = supplier.getName().getValue();
				IliImport newimport = findImportByName(oldName, modnew);
				if (newimport == null) {
					// If the old import does not exist in the new list, it eliminates it
					modold.removeClientDependency(oldimport);
				} else {
					// If it already exists, leave it
					//modold.removeClientDependency(oldimport);
					//modold.addClientDependency(newimport);
					}
			}//si no tiene nada el viejo se añade uno nuevo? 
			
			
		}
		
		Iterator newimportsi = modnew.iteratorIliImport();
		while (newimportsi.hasNext()) {
			Object obj = newimportsi.next();
			ch.ehi.interlis.modeltopicclass.IliImport newimport = (ch.ehi.interlis.modeltopicclass.IliImport) obj;
			ModelElement supplier = (ModelElement)newimport.iteratorSupplier().next();
			String newName = supplier.getName().getValue();
			IliImport oldimport = findImportByName(newName, modold);
			if (oldimport == null) {
				// If doesn't exist add
				modold.addClientDependency(newimport);
			} else {
				// si existe
			}
		}
	}

	/**
	 * Search IliImport in a given model
	 * @param findname
	 * @param modnew
	 * @return
	 */
	private IliImport findImportByName(String findname, ModelDef modnew) {
		Iterator it = modnew.iteratorClientDependency();
		while (it.hasNext()) {
			Object obj = it.next();
			if (obj instanceof ch.ehi.interlis.modeltopicclass.IliImport) {				
				ch.ehi.interlis.modeltopicclass.IliImport _import = (ch.ehi.interlis.modeltopicclass.IliImport) obj;
				if (_import.sizeSupplier() != 0) {
					ModelElement supplier = (ModelElement)_import.iteratorSupplier().next();
					String name = supplier.getName().getValue();				
					if (name.equals(findname)) {
						return _import;
					}
				}
				else {
					return null; // no hay supplier
				}
				
			} else {
				// do something with class
			}
		}
		return null;
	}

	/**
	 * Update parameters or attributes from Model definition
	 * @param modold
	 * @param modnew
	 */
	private void updateAtributesOfModelDef(ModelDef modold, ModelDef modnew) {
		modold.setKind(modnew.getKind()); // tipo
		modold.setDocumentation(modnew.getDocumentation()); // description
		modold.setIssuerURI(modnew.getIssuerURI()); // issuer URI
		modold.setVersion(modnew.getVersion());
		modold.setVersionComment(modnew.getVersionComment());
		modold.setContracted(modnew.isContracted());
		modold.setBaseLanguage(modnew.getBaseLanguage()); // Languaje
		//Technical contact
		//GeoIV Identificator
		//Further Documentation
		
	}

	/**
	 * Update Class definitions 
	 * @param clsOld
	 * @param clsNew
	 */
	public void updateClassDef(ClassDef clsOld, ClassDef clsNew) {
		clsOld.setMetaAttrb(clsNew.getMetaAttrb());
		clsOld.setDocumentation(clsNew.getDocumentation());
		clsOld.setAbstract(clsNew.isAbstract());
		clsOld.setPropFinal(clsNew.isPropFinal());
		
		updateExtendsOfClassDef(clsOld, clsNew);
		clsOld.setKind(clsNew.getKind());
		clsOld.setMetaMapping(clsNew.getMetaMapping());
		//- by now parameter
		updateParemeterDef(clsOld,clsNew);
		
		updateConstraintDef(clsOld,clsNew);
		List oldAttributechildi = AbstractClassDefUtility.getIliAttributes(clsOld);
		List newAttributechildi = AbstractClassDefUtility.getIliAttributes(clsNew);
		addNewAttributes(clsOld, oldAttributechildi, newAttributechildi);
		removeAndUpdateOldAttributes(clsOld, oldAttributechildi, newAttributechildi);
	}

	private void updateParemeterDef(ClassDef clsOld, ClassDef clsNew) {
		//clsOld.clearParameterDef();
		Iterator iterator = clsOld.iteratorParameterDef();
		while (iterator.hasNext()) {
			Object obj = iterator.next();
			
			if(obj instanceof ParameterDef) {
				ParameterDef oldparameter = (ParameterDef) obj;
				String oldSyntax = oldparameter.getSyntax().getValue();
				ParameterDef newparameter = findParameterBySyntax(oldSyntax, clsNew);
				if (newparameter == null) {
					
					clsOld.removeParameterDef(oldparameter);
				} else {
				}
				
			} else {
				// nothing to do
			}
		}
		Iterator newparamsi = clsNew.iteratorParameterDef();
				while (newparamsi.hasNext()) {
				Object obj = newparamsi.next();
				ParameterDef newparam = (ParameterDef) obj;
				
				
				String newSyntax = newparam.getSyntax().getValue();
				ParameterDef oldparam = findParameterBySyntax(newSyntax, clsOld);
				if (oldparam == null) {
					// si no existe lo agrega
					clsOld.addParameterDef(newparam);	
				} else {
						// si existe
					}
				}
	}

	private ParameterDef findParameterBySyntax(String findSyntax, ClassDef clsNew) {
		Iterator it = clsNew.iteratorParameterDef();
		while (it.hasNext()) {
			Object obj = it.next();
			if (obj instanceof ParameterDef) {				
				ParameterDef param = (ParameterDef) obj;
				
				String name = param.getSyntax().getValue();				
				if (name.equals(findSyntax)) {
					return param;
				}
			} else {
				// nothing to do
 			}
 		}
		return null;
	}

	private void updateConstraintDef(ClassDef clsOld, ClassDef clsNew) {
		Iterator oldchildi = clsNew.iteratorConstraint();
		while (oldchildi.hasNext()) {
			Object obj = oldchildi.next();
			if (obj instanceof ConstraintDef) {
				ConstraintDef restriccion = (ConstraintDef) obj;
				// clsOld.clearConstraint();
				
				restriccion.setMetaName(restriccion.getMetaName());
				restriccion.setMetaMsg(restriccion.getMetaMsg());
				clsOld.addConstraint(restriccion);
			} else {
				// ignore others; should not have others
			}
		}
		
	}

	private void updateExtendsOfClassDef(ClassDef clsOld, ClassDef clsNew) {
		Iterator extendsi = clsNew.iteratorGeneralization();
		while (extendsi.hasNext()) {
			Object obj = extendsi.next();
			if (obj instanceof ch.ehi.interlis.modeltopicclass.ClassExtends) {
				ch.ehi.interlis.modeltopicclass.ClassExtends extend = (ch.ehi.interlis.modeltopicclass.ClassExtends) obj;
				if (extend.containsParent()) {
					clsOld.clearGeneralization();
					clsOld.addGeneralization(extend);
				}
			}
		}
	}

	/**
	 * Delete and update old attribute definitions
	 * @param clsOld
	 * @param oldAttributechildi
	 * @param newAttributechildi
	 */
	private void removeAndUpdateOldAttributes(ClassDef clsOld, List oldAttributechildi, List newAttributechildi) {
		for (int i = 0; i < oldAttributechildi.size(); i++) {
			AbstractModelElement oldAttribute = (AbstractModelElement) oldAttributechildi.get(i);
			String oldName = oldAttribute.getName().getValue();
			int newIndex = findAttributeIndexInListByName(oldName, newAttributechildi); //search old attribute in new list
			if(newIndex == -1) { //elimina atributo porque no encuentra en la nueva lista
				LauncherView.getInstance().log(rsrc.getString("CIFuncDesc"),"Deleting "+oldName+" ...");
				if(oldAttribute instanceof AttributeDef) {
					//oldAttributechildi.remove(oldAttribute);
					clsOld.removeFeature((Feature) oldAttribute);
				}
				else if(oldAttribute instanceof RoleDef) {
					oldAttributechildi.remove(oldAttribute);
				}
				
			}
			else {// como se que ya existe el atributo en ambas listas, voy a actualizar
				if(oldAttribute instanceof AttributeDef) {
					AttributeDef oldAttr = (AttributeDef) oldAttribute;
					AttributeDef newAttr = (AttributeDef) newAttributechildi.get(newIndex);
					updateAttributeDef(oldAttr, newAttr);
				}
				else if(oldAttribute instanceof RoleDef) {
					RoleDef oldRole = (RoleDef) oldAttribute;
					RoleDef newRole = (RoleDef) newAttributechildi.get(newIndex);
					updateRoleDef(oldRole, newRole);
				}
			}
			
		}
		
	}

	/**
	 * Update parameters from role definitions
	 * @param oldRole
	 * @param newRole
	 */
	private void updateRoleDef(RoleDef oldRole, RoleDef newRole) {
		oldRole.setIliAttributeKind(newRole.getIliAttributeKind());
		oldRole.setDocumentation(newRole.getDocumentation());
		oldRole.setAbstract(newRole.isAbstract());
		oldRole.setPropFinal(newRole.isPropFinal());
		oldRole.setOrdering(newRole.getOrdering());
		oldRole.setPropExternal(newRole.isPropExternal());
		oldRole.setNavigable(newRole.isNavigable());
		oldRole.setPropExtended(newRole.isPropExtended());
		oldRole.setMultiplicity(newRole.getMultiplicity());
		
		if(newRole.containsParticipant() && (oldRole.containsParticipant() == false)) {
			oldRole.attachParticipant(newRole.detachParticipant()); //clases implicadas
		}
		// DerivedFrom 
		if (newRole.containsRoleDefDerived()) {
			oldRole.attachRoleDefDerived(newRole.detachRoleDefDerived());
		} else {
		
		}
	}

	/**
	 * Update parameters from attribute definitions
	 * @param oldAttr
	 * @param newAttr
	 */
	private void updateAttributeDef(AttributeDef oldAttr, AttributeDef newAttr) {
		oldAttr.setMetaAttrb(newAttr.getMetaAttrb());
		//Type not found
		DomainAttribute attrType = updateDomainAtr(oldAttr, newAttr);
		if(attrType != null) {
			oldAttr.detachAttrType();
			oldAttr.attachAttrType(attrType);
		}
		
		oldAttr.setDocumentation(newAttr.getDocumentation());
		oldAttr.setAbstract(newAttr.isAbstract());
		oldAttr.setPropFinal(newAttr.isPropFinal());
		oldAttr.setPropTransient(newAttr.isPropTransient());
		oldAttr.setPropExtended(newAttr.isPropExtended());
		oldAttr.setOrdering(newAttr.getOrdering());
		oldAttr.setMultiplicity(newAttr.getMultiplicity());
		
		AttributeValueUsage attributeValue = updateAttributeValueUsage(oldAttr, newAttr);
		if(attributeValue != null) {
			oldAttr.detachAttributeValueUsage();
			oldAttr.attachAttributeValueUsage(attributeValue);
		}
	}


	private AttributeValueUsage updateAttributeValueUsage(AttributeDef oldAttr, AttributeDef newAttr) {
		if(newAttr.containsAttributeValueUsage()) {			
			AttributeValueUsage attrType = (AttributeValueUsage) newAttr.getAttributeValueUsage();
			return attrType;
		}
		return null;
	}

	private DomainAttribute updateDomainAtr(AttributeDef oldAttr, AttributeDef newAttr) {
		if(newAttr.containsAttrType()) {			
			DomainAttribute attrType = (DomainAttribute) newAttr.getAttrType();
			return attrType;
		}
		return null;
	}

	/**
	 * Search attribute definition index in a given list with name parameter
	 * @param oldName
	 * @param newAttributechildi
	 * @return
	 */
	private int findAttributeIndexInListByName(String oldName, List newAttributechildi) {
		for (int i = 0; i < newAttributechildi.size(); i++) {
			AbstractModelElement modnew = (AbstractModelElement) newAttributechildi.get(i);
			String newName = modnew.getName().getValue();	
			if (newName.equals(oldName)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Add new attribute definitions to old class
	 * @param clsOld
	 * @param oldAttributechildi
	 * @param newAttributechildi
	 */
	private void addNewAttributes(ClassDef clsOld, List oldAttributechildi, List newAttributechildi) {
		
		for (int i = 0; i < newAttributechildi.size(); i++) {
			ModelElement attributeNew = (ModelElement) newAttributechildi.get(i);
			String newName = attributeNew.getName().getValue();		
			int oldIndex = findIndexInListByName(newName, oldAttributechildi);
			
			if (oldIndex == -1) { // si no esta en la lista vieja se agrega
				LauncherView.getInstance().log(rsrc.getString("CIFuncDesc"),"Creating "+newName+" ...");
				if (attributeNew instanceof AttributeDef) {
					clsOld.addFeature((Feature) attributeNew);	
				}
				else {
					
				}
			}
		}		
	}

	/**
	 * Update parameters of domain definitions
	 * @param oldDomain
	 * @param newDomain
	 */
	public void updateDomainDef(DomainDef oldDomain, DomainDef newDomain) {
		oldDomain.setMetaAttrb(newDomain.getMetaAttrb());
		
		Type type = updateType(oldDomain,newDomain);
		if(type != null) {
			oldDomain.detachType();
			oldDomain.attachType(type);
		}
		oldDomain.setDocumentation(newDomain.getDocumentation());
		oldDomain.setAbstract(newDomain.isAbstract());
		oldDomain.setPropFinal(newDomain.isPropFinal());
		oldDomain.setMandatory(newDomain.isMandatory());
		
		updateSpecialized(oldDomain, newDomain);

	}
	
	private Type updateType(DomainDef oldDomain, DomainDef newDomain) {

		if(newDomain.containsType()) {
			
			Type actual = newDomain.getType();
			return actual;
		}
		
		return null;
	}

	private void updateSpecialized(DomainDef oldDomain, DomainDef newDomain) {
		Iterator extendsi = newDomain.iteratorGeneralization();
		while (extendsi.hasNext()) {
			Object obj = extendsi.next();
			if (obj instanceof ch.ehi.interlis.domainsandconstants.DomainExtends) {
				ch.ehi.interlis.domainsandconstants.DomainExtends extend = (ch.ehi.interlis.domainsandconstants.DomainExtends) obj;
				if (extend.containsParent()) {
					DomainDef supplier = (DomainDef) extend.getParent();
					oldDomain.clearGeneralization();
					oldDomain.addGeneralization(extend);
				}
			}
		}
		
	}

	/**
	 * Update parameters of MetadataUse definitions
	 * @param mtdOld
	 * @param mtdNew
	 */
	public void updateMetaDataUseDef(MetaDataUseDef mtdOld, MetaDataUseDef mtdNew) {
		mtdOld.setBasketOid(mtdNew.getBasketOid());
		mtdOld.setKind(mtdNew.getKind());
		mtdOld.setDocumentation(mtdNew.getDocumentation());
		mtdOld.setSyntax(mtdNew.getSyntax()); // Definition?? check later
	}
	
	/**
	 * Update parameters of Unit definitions 
	 * @param untOld
	 * @param untNew
	 */
	public void updateUnitDef(UnitDef untOld, UnitDef untNew) {
		untOld.setDescName(untNew.getDescName());
		untOld.setDocumentation(untNew.getDocumentation());
		untOld.setSyntax(untNew.getSyntax());
		// cant access to depend on (umleditor hace esto automaticamente)
	}

	/**
	 * Update parameters of Association definitions
	 * @param asoOld
	 * @param asoNew
	 */
	public void updateAssociationDef(AssociationDef asoOld, AssociationDef asoNew) {
		asoOld.setDocumentation(asoNew.getDocumentation());
		asoOld.setAbstract(asoNew.isAbstract());
		asoOld.setPropFinal(asoNew.isPropFinal());

		updateExtendsOfAssociationDef(asoOld, asoNew);
		updateDerivedFrom(asoOld, asoNew);
	
		updateAssociationRoles(asoOld,asoNew);
		/*
		 * Check roles from associationdef
		 */
		List oldAttributechildi = AbstractClassDefUtility.getIliAttributes(asoOld);
		List newAttributechildi = AbstractClassDefUtility.getIliAttributes(asoNew);
		addNewAttributesInAssociation(asoOld, oldAttributechildi, newAttributechildi);
		removeAndUpdateOldAttributesInAssociation(asoOld, oldAttributechildi, newAttributechildi);
		
		updateConstraintDef(asoOld, asoNew);
	}
	
	private void updateAssociationRoles(AssociationDef asoOld, AssociationDef asoNew) {
		
		Iterator childi = asoOld.iteratorConnection();
		while (childi.hasNext()) {
			Object obj = childi.next();
			if (obj instanceof RoleDef) {
				RoleDef oldrole = (RoleDef) obj;
				String oldname = oldrole.getName().getValue();
				RoleDef newrole = findRoleByName(oldname, asoNew);
				
				if(newrole == null) {
					asoOld.removeConnection(oldrole);
				} else {
					updateRoleDef(oldrole, newrole);
				}
			} else {
				// ignore others; should not have others
			}
		}
		
		Iterator newrolesi = asoNew.iteratorConnection();
		while (newrolesi.hasNext()) {
			Object obj = newrolesi.next();
			RoleDef newrole = (RoleDef) obj;
			
			String newName = newrole.getName().getValue();
			RoleDef oldrole = findRoleByName(newName, asoOld);
			if (oldrole == null) {
				// si no existe lo agrega
				asoOld.addConnection(newrole);
			} else {
				// si existe
			}
		}
	}

	private RoleDef findRoleByName(String oldname, AssociationDef asoNew) {
		Iterator it = asoNew.iteratorConnection();
		while (it.hasNext()) {
			Object obj = it.next();
			if (obj instanceof RoleDef) {				
				RoleDef rol = (RoleDef) obj;
				
				String name = rol.getName().getValue();				
				if (name.equals(oldname)) {
					return rol;
				}
			} else {
				// nothing to do
 			}
 		}
		return null;
	}

	private void updateConstraintDef(AssociationDef asoOld, AssociationDef asoNew) {
		Iterator oldchildi = asoNew.iteratorConstraint();
		while (oldchildi.hasNext()) {
			Object obj = oldchildi.next();
			if (obj instanceof ConstraintDef) {
				ConstraintDef restriccion = (ConstraintDef) obj;
				// clsOld.clearConstraint();
				
				restriccion.setMetaName(restriccion.getMetaName());
				restriccion.setMetaMsg(restriccion.getMetaMsg());
				asoOld.addConstraint(restriccion);
			} else {
				// ignore others; should not have others
			}
		}
	}

	private void updateExtendsOfAssociationDef(AssociationDef asoOld, AssociationDef asoNew) {
		Iterator extendsi = asoNew.iteratorGeneralization();
		while (extendsi.hasNext()) {
			Object obj = extendsi.next();
			if (obj instanceof ch.ehi.interlis.modeltopicclass.ClassExtends) {
				ch.ehi.interlis.modeltopicclass.ClassExtends extend = (ch.ehi.interlis.modeltopicclass.ClassExtends) obj;
				if (extend.containsParent()) {
					asoOld.clearGeneralization();
					asoOld.addGeneralization(extend);
				}
			}
		}
		
	}

	private void updateDerivedFrom(AssociationDef asoOld, AssociationDef asoNew) {
		Iterator derivedi = asoNew.iteratorClientDependency();
		while (derivedi.hasNext()) {
			Object obj = derivedi.next();
			if (obj instanceof ch.ehi.interlis.associations.AssociationDefDerived) {
				ch.ehi.interlis.associations.AssociationDefDerived derived = (ch.ehi.interlis.associations.AssociationDefDerived) obj;
				if (derived.sizeSupplier() > 0) {
					ViewableDef supplier = (ViewableDef) derived.iteratorSupplier().next();
					asoOld.clearClientDependency();
					asoOld.addClientDependency(derived);
				}
			}
		}
		
	}

	private void removeAndUpdateOldAttributesInAssociation(AssociationDef asoOld, List oldAttributechildi,
			List newAttributechildi) {
		for (int i = 0; i < oldAttributechildi.size(); i++) {
			AbstractModelElement oldAttribute = (AbstractModelElement) oldAttributechildi.get(i);
			String oldName = oldAttribute.getName().getValue();
			int newIndex = findAttributeIndexInListByName(oldName, newAttributechildi); //search old attribute in new list
			if(newIndex == -1) { //elimina atributo porque no encuentra en la nueva lista
				LauncherView.getInstance().log(rsrc.getString("CIFuncDesc"),"Deleting "+ oldName+" ...");
				if(oldAttribute instanceof AttributeDef) {
					//oldAttributechildi.remove(oldAttribute);
					asoOld.removeFeature((Feature) oldAttribute);
				}
				else if(oldAttribute instanceof RoleDef) {
					asoOld.removeFeature((Feature)oldAttribute);
				}
				
			}
			else {// como se que ya existe el atributo en ambas listas, voy a actualizar
				if(oldAttribute instanceof AttributeDef) {
					AttributeDef oldAttr = (AttributeDef) oldAttribute;
					AttributeDef newAttr = (AttributeDef) newAttributechildi.get(newIndex);
					LauncherView.getInstance().log(rsrc.getString("CIFuncDesc"),"Updating "+ oldAttr.getName().getValue()+" ...");
					updateAttributeDef(oldAttr, newAttr);
				}
				else if(oldAttribute instanceof RoleDef) {
					RoleDef oldRole = (RoleDef) oldAttribute;
					RoleDef newRole = (RoleDef) newAttributechildi.get(newIndex);
					LauncherView.getInstance().log(rsrc.getString("CIFuncDesc"),"Updating "+ oldRole.getName().getValue()+" ...");
					updateRoleDef(oldRole, newRole);
				}
			}
			
}
	}

	private void addNewAttributesInAssociation(AssociationDef asoOld, List oldAttributechildi,
			List newAttributechildi) {
		for (int i = 0; i < newAttributechildi.size(); i++) {
			ModelElement attributeNew = (ModelElement) newAttributechildi.get(i);
			String newName = attributeNew.getName().getValue();		
			int oldIndex = findIndexInListByName(newName, oldAttributechildi);
			
			if (oldIndex == -1) { // si no esta en la lista vieja se agrega
				LauncherView.getInstance().log(rsrc.getString("CIFuncDesc"),"Creating "+newName+" ...");
				if (attributeNew instanceof AttributeDef) {
					//clsOld.addOwnedElement(attributeNew);
					asoOld.addFeature((Feature) attributeNew);	
				}
				else if(attributeNew instanceof RoleDef) {
					asoOld.addFeature((Feature) attributeNew);
				}
				else {
					
				}
			}
} 
	}

	/**
	 * Update parameters of View definitions
	 * @param viewOld
	 * @param viewNew
	 */
	public void updateViewDef(ViewDef viewOld, ViewDef viewNew) {
		viewOld.setDocumentation(viewNew.getDocumentation());
		viewOld.setSyntax(viewNew.getSyntax());
		// Depends on 
	}
	
	/**
	 * Update parameters of graphic definitions
	 * @param graphOld
	 * @param graphNew
	 */
	public void updateGraphicDef(GraphicDef graphOld, GraphicDef graphNew){
		graphOld.setDocumentation(graphNew.getDocumentation());
		graphOld.setSyntax(graphNew.getSyntax());
		// Depends on 
	}
	
	/**
	 * Called whenever the part throws an exception.
	 * 
	 * @param exception
	 *            java.lang.Throwable
	 */
	protected void handleException(java.lang.Throwable exception) {
		LauncherView.getInstance().handleException(exception);
	}

}
