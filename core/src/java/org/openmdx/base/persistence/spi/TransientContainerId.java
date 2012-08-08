/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: TransientContainerId.java,v 1.1 2010/01/26 15:42:54 hburger Exp $
 * Description: TransientContainerId 
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/01/26 15:42:54 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2010, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * ------------------
 * 
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.base.persistence.spi;

import java.util.UUID;

import org.openmdx.base.naming.Path;

/**
 * TransientContainerId
 */
public final class TransientContainerId {

    /**
     * Constructor 
     *
     * @param parent
     * @param feature
     */
    public TransientContainerId(
        UUID parent, 
        String feature
    ) {
        this.parent = parent;
        this.feature = feature;
    }

    /**
     * The parent's transient object id
     */
    private final UUID parent;
    
    /**
     * The container's feature name
     */
    private final String feature;
    
    /**
     * The transient container id's path representation
     */
    private transient Path xri;
    
    /**
    private transient String string;
    
    /**
     * Retrieve parent.
     *
     * @return Returns the parent.
     */
    public UUID getParent() {
        return this.parent;
    }
    
    /**
     * Retrieve feature.
     *
     * @return Returns the feature.
     */
    public String getFeature() {
        return this.feature;
    }

    /***
     * Retrieve the transient container id's path representation
     * 
     * @return the transient container id's path representation
     */
    public Path toPath(
    ){  
        if(this.xri == null) {
            this.xri = new Path(this.parent).add(this.feature);
        }
        return this.xri;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        } else if (obj instanceof TransientContainerId) {
            TransientContainerId that = (TransientContainerId) obj;
            return this.parent.equals(that.parent) && this.feature.equals(that.feature);
        } else {
            return false;
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.parent.hashCode() + this.feature.hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return toPath().toXRI();
    }
    
}