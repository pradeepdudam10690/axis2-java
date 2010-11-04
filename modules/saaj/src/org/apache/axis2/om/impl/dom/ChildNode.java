/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.axis2.om.impl.dom;

import org.apache.axis2.om.OMContainer;
import org.apache.axis2.om.OMException;
import org.apache.axis2.om.OMNode;
import org.apache.axis2.om.impl.OMNodeEx;
import org.w3c.dom.Node;

/**
 * @author Ruchith Fernando (ruchith.fernando@gmail.com)
 */
public abstract class ChildNode extends NodeImpl {

	protected ChildNode previousSubling;
	
	protected ChildNode nextSibling;
	
	protected ParentNode parentNode;
	
	
	/**
	 * @param ownerNode
	 */
	protected ChildNode(DocumentImpl ownerDocument) {
		super(ownerDocument);
	}
	
	protected ChildNode() {
		
	}
	
	public OMNode getNextOMSibling() throws OMException {
		return this.nextSibling;
	}
	public Node getNextSibling() {
		return this.nextSibling;
	}
	public OMNode getPreviousOMSibling() {
		return this.previousSubling;
	}
	public Node getPreviousSibling() {
		return this.previousSubling;
	}

	///
	///OMNode methods
	///
	public void setNextOMSibling(OMNode node) {
		if(node instanceof ChildNode)
			this.nextSibling = (ChildNode)node;
		else
			throw new OMException("The node is not a " + ChildNode.class);
	}

	public void setPreviousOMSibling(OMNode node) {
		if(node instanceof ChildNode)
			this.previousSubling = (ChildNode)node;
		else
			throw new OMException("The node is not a " + ChildNode.class);		
	}
	
	public OMContainer getParent() throws OMException {
		return (OMContainer)this.parentNode;
	}
	
	public Node getParentNode() {
		return this.parentNode;
	}
	
	public void setParent(OMContainer element) {
		if(element instanceof ParentNode)
			this.parentNode = (ParentNode)element;
		else
			throw new OMException("The given parent is not of the type " + ParentNode.class);

	}
	
	public OMNode detach() throws OMException{
		if(this.parentNode == null) {
			throw new OMException("Parent level elements cannot be ditached");
		} else {
			if(previousSubling == null) { // This is the first child
				this.parentNode.setFirstChild(nextSibling);
			} else {
				((OMNodeEx)this.getPreviousOMSibling()).setNextOMSibling(nextSibling);
			} if (this.nextSibling != null) {
				this.nextSibling.setPreviousOMSibling(this.previousSubling);
			}
			this.parentNode = null; 
		}
		return this;
	}
	
	public void discard() throws OMException {
		//TODO
		throw new UnsupportedOperationException("TODO");
	}
	
	/**
	 * Insert the given sibling next to this item
	 */
	public void insertSiblingAfter(OMNode sibling) throws OMException {
		
		if(this.parentNode != null) {
			((OMNodeEx)sibling).setParent(this.parentNode);
		}
		
		if(sibling instanceof ChildNode) {
			ChildNode domSibling = (ChildNode)sibling;
			domSibling.previousSubling = this;
			if(this.nextSibling != null) {
				this.nextSibling.previousSubling = domSibling;
			}
			domSibling.nextSibling = this.nextSibling;
			this.nextSibling = domSibling;
			
		} else {
			throw new OMException("The given child is not of type " + ChildNode.class);
		}
	}
	
	/**
	 * Insert the given sibling before this item
	 */
	public void insertSiblingBefore(OMNode sibling) throws OMException {
		((OMNodeEx)sibling).setParent(this.parentNode);
		if(sibling instanceof ChildNode) {
			ChildNode domSibling = (ChildNode)sibling;
			domSibling.nextSibling = this;
			if(this.previousSubling != null) {
				this.previousSubling.nextSibling = domSibling;
			}
			domSibling.previousSubling = this.previousSubling;
			this.previousSubling = domSibling;
			
		} else {
			throw new OMException("The given child is not of type " + ChildNode.class);
		}
		
	}

}