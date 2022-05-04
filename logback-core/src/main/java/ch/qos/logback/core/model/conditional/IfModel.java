/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2022, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.core.model.conditional;

import ch.qos.logback.core.model.Model;

public class IfModel extends Model {

    private static final long serialVersionUID = 1516046821762377019L;

    public enum BranchState {IN_ERROR, IF_BRANCH, ELSE_BRANCH; }
    
    String condition;
    BranchState branchState = null;
    
    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }
    
    public BranchState getBranchState() {
        return branchState;
    }
    
    public void setBranchState(BranchState state) {
        this.branchState = state;
    }
    

    public void setBranchState(boolean booleanProxy) {
        if(booleanProxy)
            setBranchState(BranchState.IF_BRANCH);
        else 
            setBranchState(BranchState.ELSE_BRANCH);
    }
    
    public void resetBranchState() {
        setBranchState(null);
    }
        

}
