/*
 * Copyright (c) 2010. Axon Framework
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.axonframework.unitofwork;

import org.axonframework.domain.AggregateRoot;
import org.axonframework.domain.Event;
import org.axonframework.eventhandling.EventBus;

/**
 * This class represents a UnitOfWork in which modifications are made to aggregates. A typical UnitOfWork scope is the
 * execution of a command. A UnitOfWork may be used to prevent individual events from being published before a number of
 * aggregates has been processed. It also allows repositories to manage resources, such as locks, over an entire
 * transaction. Locks, for example, will only be released when the UnitOfWork is either committed or rolled back.
 * <p/>
 * The current UnitOfWork can be obtained using {@link CurrentUnitOfWork#get()}.
 *
 * @author Allard Buijze
 * @see CurrentUnitOfWork
 * @since 0.6
 */
public interface UnitOfWork {

    /**
     * Register an aggregate with this UnitOfWork. These aggregates will be saved (at the latest) when the UnitOfWork is
     * committed.
     *
     * @param aggregateRoot         The aggregate root to register in the UnitOfWork
     * @param saveAggregateCallback The callback that is invoked when the UnitOfWork wants to store the registered
     *                              aggregate
     * @param <T>                   The type of aggregate
     */
    <T extends AggregateRoot> void registerAggregate(T aggregateRoot, SaveAggregateCallback<T> saveAggregateCallback);

    /**
     * Reports the fact that a repository has stored an aggregate. This could either be as part of committing the
     * UnitOfWork, or by an explicit call by the command handling code.
     *
     * @param aggregateRoot The aggregate root that has been saved.
     * @param <T>           The type of aggregate
     */
    <T extends AggregateRoot> void reportAggregateSaved(T aggregateRoot);

    /**
     * Request to publish the given <code>event</code> on the given <code>eventBus</code>. The UnitOfWork may either
     * publish immediately, or buffer the events until the UnitOfWork is committed.
     *
     * @param event    The event to be published on the event bus
     * @param eventBus The event bus on which to publish the event
     */
    void publishEvent(Event event, EventBus eventBus);

    /**
     * Clear the UnitOfWork of any buffered changes. All buffered events and registered aggregates are discarded and
     * registered {@link UnitOfWorkListener}s are notified.
     */
    void rollback();

    /**
     * Commits the UnitOfWork. All registered aggregates that have not been registered as stored are saved in their
     * respective repositories, buffered events are sent to their respective event bus, and all registered
     * UnitOfWorkListeners are notified.
     */
    void commit();

    /**
     * Register a listener that listens to state changes in this UnitOfWork. This typically allows components to clean
     * up resources, such as locks, when a UnitOfWork is committed or rolled back.
     *
     * @param listener The listener to notify when the UnitOfWork's state changes.
     */
    void registerListener(UnitOfWorkListener listener);
}