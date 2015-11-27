/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
package io.atomix.copycat.client;

import io.atomix.catalyst.transport.Address;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Strategies for managing how clients connect to and communicate with the cluster.
 * <p>
 * Connection strategies manage which servers a client attempts to contact and submit operations
 * to. Clients can communicate with followers, leaders, or both. Connection strategies offer the
 * option for clients to spread connections across the cluster for scalability or connect to the
 * cluster's leader for performance.
 *
 * @author <a href="http://github.com/kuujo>Jordan Halterman</a>
 */
public enum ConnectionStrategies implements ConnectionStrategy {

  /**
   * The {@code ANY} connection strategy allows the client to connect to any server in the cluster. Clients
   * will attempt to connect to a random server, and the client will persist its connection with the first server
   * through which it is able to communicate. If the client becomes disconnected from a server, it will attempt
   * to connect to the next random server again.
   */
  ANY {
    @Override
    public List<Address> getConnections(Address leader, List<Address> servers) {
      return servers;
    }
  },

  /**
   * The {@code LEADER} connection strategy forces the client to attempt to connect to the cluster's leader.
   * Connecting to the leader means the client's operations are always handled by the first server to receive
   * them. However, clients connected to the leader will not significantly benefit from {@link Query queries}
   * with lower consistency levels, and more clients connected to the leader could mean more load on a single
   * point in the cluster.
   * <p>
   * If the client is unable to find a leader in the cluster, the client will connect to a random server.
   */
  LEADER {
    @Override
    public List<Address> getConnections(Address leader, List<Address> servers) {
      return leader != null ? Collections.singletonList(leader) : servers;
    }
  },

  /**
   * The {@code FOLLOWERS} connection strategy forces the client to connect only to followers. Connecting to
   * followers ensures that the leader is not overloaded with direct client requests. This strategy should be
   * used when clients frequently submit {@link Query queries} with lower consistency levels that don't need to
   * be forwarded to the cluster leader. For clients that frequently submit commands or queries with linearizable
   * consistency, the {@link #LEADER} ConnectionStrategy may be more performant.
   */
  FOLLOWERS {
    @Override
    public List<Address> getConnections(Address leader, List<Address> servers) {
      return servers.size() > 1 ? servers.stream().filter(a -> !a.equals(leader)).collect(Collectors.toList()) : servers;
    }
  }

}
