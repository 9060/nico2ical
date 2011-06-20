package org.ryu22e.nico2cal.model;

import java.io.Serializable;
import java.util.List;

import org.slim3.datastore.Attribute;
import org.slim3.datastore.Model;

import com.google.appengine.api.datastore.Key;

/**
 * @author ryu22e
 *
 */
@Model(schemaVersion = 1)
public final class NicoliveIndex implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    @Attribute(primaryKey = true)
    private Key key;

    /**
     * 
     */
    @Attribute(version = true)
    private Long version;

    /**
     * 
     */
    private String keyword;

    /**
     * 
     */
    private List<Key> nicoliveKeys;

    /**
     * Returns the key.
     *
     * @return the key
     */
    public Key getKey() {
        return key;
    }

    /**
     * Sets the key.
     *
     * @param key
     *            the key
     */
    public void setKey(Key key) {
        this.key = key;
    }

    /**
     * Returns the version.
     *
     * @return the version
     */
    public Long getVersion() {
        return version;
    }

    /**
     * Sets the version.
     *
     * @param version
     *            the version
     */
    public void setVersion(Long version) {
        this.version = version;
    }

    /**
     * @return the keyword
     */
    public String getKeyword() {
        return keyword;
    }

    /**
     * @param keyword the keyword to set
     */
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    /**
     * @return the nicoliveKeys
     */
    public List<Key> getNicoliveKeys() {
        return nicoliveKeys;
    }

    /**
     * @param nicoliveKeys the nicoliveKeys to set
     */
    public void setNicoliveKeys(List<Key> nicoliveKeys) {
        this.nicoliveKeys = nicoliveKeys;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        NicoliveIndex other = (NicoliveIndex) obj;
        if (key == null) {
            if (other.key != null) {
                return false;
            }
        } else if (!key.equals(other.key)) {
            return false;
        }
        return true;
    }
}
