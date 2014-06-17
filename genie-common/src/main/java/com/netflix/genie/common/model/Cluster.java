/*
 *
 *  Copyright 2014 Netflix, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.netflix.genie.common.model;

import com.netflix.genie.common.exceptions.CloudServiceException;
import com.netflix.genie.common.model.Types.ClusterStatus;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Representation of the state of the Cluster object.
 *
 * @author skrishnan
 * @author amsharma
 * @author tgianos
 */
@Entity
@Table(schema = "genie")
@Cacheable(false)
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Cluster extends Auditable implements Serializable {

    private static final long serialVersionUID = 8046582926818942370L;
    private static final Logger LOG = LoggerFactory.getLogger(Cluster.class);

    /**
     * Name for this cluster, e.g. cquery.
     */
    @Basic(optional = false)
    private String name;

    /**
     * User who created this cluster.
     */
    @Basic(optional = false)
    private String user;

    /**
     * Status of cluster - UP, OUT_OF_SERVICE or TERMINATED.
     */
    @Basic(optional = false)
    @Enumerated(EnumType.STRING)
    private ClusterStatus status;

    /**
     * The type of the cluster to use to figure out the job manager for this cluster.
     * eg: yarn, presto, mesos etc. The mapping JobManager will be specified using 
     * the property:  netflix.genie.server.<clusterType>.JobManagerImpl
     */
    @Basic(optional = false)
    private String clusterType;

    /**
     * Version of this cluster.
     */
    @Basic
    @Column(name = "clusterVersion")
    private String version;

    /**
     * Reference to all the configuration (xml's) needed for this cluster.
     */
    @XmlElementWrapper(name = "configs")
    @XmlElement(name = "config")
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> configs;

    /**
     * Set of tags for scheduling - e.g. adhoc, sla, vpc etc.
     */
    @XmlElementWrapper(name = "tags")
    @XmlElement(name = "tag")
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> tags;

    /**
     * Commands supported on this cluster - e.g. prodhive, testhive, etc.
     */
    @XmlElementWrapper(name = "commands")
    @XmlElement(name = "command")
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Command> commands;

    /**
     * Default Constructor.
     */
    public Cluster() {
        super();
    }

    /**
     * Construct a new Cluster.
     *
     * @param name The name of the cluster. Not null/empty/blank.
     * @param user The user who created the cluster. Not null/empty/blank.
     * @param status The status of the cluster. Not null.
     * @param clusterType The type of the cluster. Not null/empty/blank.
     * @param configs The configuration files for the cluster. Not null or
     * empty.
     * @throws CloudServiceException
     */
    public Cluster(
            final String name,
            final String user,
            final ClusterStatus status,
            final String clusterType,
            final Set<String> configs) throws CloudServiceException {
        super();
        this.name = name;
        this.user = user;
        this.status = status;
        this.clusterType = clusterType;
        this.configs = configs;
    }

    /**
     * Check to make sure everything is OK before persisting.
     *
     * @throws CloudServiceException
     */
    @PrePersist
    protected void onCreateCluster() throws CloudServiceException {
        validate(this.name, this.user, this.status, this.clusterType, this.configs);
    }

    /**
     * Gets the name for this cluster.
     *
     * @return name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name for this cluster.
     *
     * @param name name for this cluster. Not null/empty/blank.
     * @throws CloudServiceException
     */
    public void setName(final String name) throws CloudServiceException {
        if (StringUtils.isBlank(name)) {
            throw new CloudServiceException(
                    HttpURLConnection.HTTP_BAD_REQUEST,
                    "No name Entered.");
        }
        this.name = name;
    }

    /**
     * Gets the user that created this cluster.
     *
     * @return user
     */
    public String getUser() {
        return this.user;
    }

    /**
     * Sets the user who created this cluster.
     *
     * @param user user who created this cluster. Not null/empty/blank.
     * @throws CloudServiceException
     */
    public void setUser(final String user) throws CloudServiceException {
        if (StringUtils.isBlank(user)) {
            throw new CloudServiceException(
                    HttpURLConnection.HTTP_BAD_REQUEST,
                    "No user Entered.");
        }
        this.user = user;
    }

    /**
     * Gets the status for this cluster.
     *
     * @return status - possible values: Types.ConfigStatus
     */
    public ClusterStatus getStatus() {
        return status;
    }

    /**
     * Sets the status for this cluster.
     *
     * @param status The status of the cluster. Not null.
     * @throws CloudServiceException
     * @see ClusterStatus
     */
    public void setStatus(final ClusterStatus status) throws CloudServiceException {
        if (status == null) {
            throw new CloudServiceException(
                    HttpURLConnection.HTTP_BAD_REQUEST,
                    "No Status Entered.");
        }
        this.status = status;
    }

    /**
     * Get the clusterType for this cluster.
     *
     * @return clusterType: The type of the cluster like yarn, presto, mesos etc. 
     */
    public String getClusterType() {
        return clusterType;
    }

    /**
     * Set the type for this cluster.
     *
     * @param clusterType The type of this cluster. Not null/empty/blank.
     * @throws CloudServiceException
     */
    public void setClusterType(String clusterType) throws CloudServiceException {
        if (StringUtils.isBlank(clusterType)) {
            throw new CloudServiceException(
                    HttpURLConnection.HTTP_BAD_REQUEST,
                    "No cluster type Entered.");
        }
        this.clusterType = clusterType;
    }

    /**
     * Gets the version of this cluster.
     *
     * @return version
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * Sets the version for this cluster.
     *
     * @param version version number for this cluster
     */
    public void setVersion(final String version) {
        this.version = version;
    }

    /**
     * Gets the tags allocated to this cluster.
     *
     * @return the tags as an unmodifiable list
     */
    public Set<String> getTags() {
        return this.tags;
    }

    /**
     * Sets the tags allocated to this cluster.
     *
     * @param tags the tags to set. Not Null.
     * @throws CloudServiceException
     */
    public void setTags(final Set<String> tags) throws CloudServiceException {
        if (tags == null) {
            final String msg = "No tags passed in to set. Unable to continue.";
            LOG.error(msg);
            throw new CloudServiceException(HttpURLConnection.HTTP_BAD_REQUEST, msg);
        }
        this.tags = tags;
    }

    /**
     * Gets the configurations for this cluster.
     *
     * @return The cluster configurations as unmodifiable list
     */
    public Set<String> getConfigs() {
        return this.configs;
    }

    /**
     * Sets the configurations for this cluster.
     *
     * @param configs The configuration files that this cluster needs. Not
     * null/empty.
     * @throws CloudServiceException
     */
    public void setConfigs(final Set<String> configs) throws CloudServiceException {
        if (configs == null || configs.isEmpty()) {
            throw new CloudServiceException(
                    HttpURLConnection.HTTP_BAD_REQUEST,
                    "At least one config required.");
        }
        this.configs = configs;
    }

    /**
     * Gets the commands that this cluster supports.
     *
     * @return commands Not supposed to be exposed in request/response messages
     * hence marked transient.
     */
    public Set<Command> getCommands() {
        return this.commands;
    }

    /**
     * Sets the commands for this cluster.
     *
     * @param commands The commands that this cluster supports
     * @throws CloudServiceException
     */
    public void setCommands(final Set<Command> commands) throws CloudServiceException {
        if (commands == null) {
            final String msg = "No commands passed in to set. Unable to continue.";
            LOG.error(msg);
            throw new CloudServiceException(HttpURLConnection.HTTP_BAD_REQUEST, msg);
        }
        this.commands = commands;
    }

    /**
     * Check to make sure that the required parameters exist.
     *
     * @param cluster The configuration to check
     * @throws CloudServiceException
     */
    public static void validate(final Cluster cluster) throws CloudServiceException {
        if (cluster == null) {
            throw new CloudServiceException(
                    HttpURLConnection.HTTP_BAD_REQUEST,
                    "No cluster entered. Unable to validate.");
        }
        validate(
                cluster.getName(),
                cluster.getUser(),
                cluster.getStatus(),
                cluster.getClusterType(),
                cluster.getConfigs());
    }

    /**
     * Helper method to ensure that values are valid for a cluster.
     *
     * @param name The name of the cluster
     * @param user The user who created the cluster
     * @param status The status of the cluster
     * @param clusterType The type of cluster
     * @param configs The configuration files for the cluster
     * @throws CloudServiceException
     */
    private static void validate(
            final String name,
            final String user,
            final ClusterStatus status,
            final String clusterType,
            final Set<String> configs) throws CloudServiceException {
        final StringBuilder builder = new StringBuilder();
        if (StringUtils.isBlank(name)) {
            builder.append("Cluster name is missing and required.\n");
        }
        if (StringUtils.isBlank(user)) {
            builder.append("User name is missing and required.\n");
        }
        if (status == null) {
            builder.append("No cluster status entered and is required.\n");
        }
        if (StringUtils.isBlank(clusterType)) {
            builder.append("No cluster type entered and is required.\n");
        }
        if (configs == null || configs.isEmpty()) {
            builder.append("At least one configuration file is required for the cluster.\n");
        }

        if (builder.length() != 0) {
            builder.insert(0, "Cluster configuration errors:\n");
            final String msg = builder.toString();
            LOG.error(msg);
            throw new CloudServiceException(HttpURLConnection.HTTP_BAD_REQUEST, msg);
        }
    }
}
