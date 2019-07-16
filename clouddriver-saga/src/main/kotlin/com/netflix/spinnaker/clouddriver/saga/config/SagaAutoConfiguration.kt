/*
 * Copyright 2019 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.spinnaker.clouddriver.saga.config

import com.netflix.spectator.api.Registry
import com.netflix.spinnaker.clouddriver.event.EventPublisher
import com.netflix.spinnaker.clouddriver.event.config.EventSourceAutoConfiguration
import com.netflix.spinnaker.clouddriver.event.persistence.EventRepository
import com.netflix.spinnaker.clouddriver.saga.LocalSagaEventPublisher
import com.netflix.spinnaker.clouddriver.saga.SagaEventHandlerProvider
import com.netflix.spinnaker.clouddriver.saga.SagaService
import com.netflix.spinnaker.clouddriver.saga.persistence.DefaultSagaRepository
import com.netflix.spinnaker.clouddriver.saga.persistence.SagaRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary

@Configuration
@Import(EventSourceAutoConfiguration::class)
@EnableConfigurationProperties(SagaProperties::class)
class SagaAutoConfiguration {

  @Bean
  fun sagaEventHandlerProvider(): SagaEventHandlerProvider = SagaEventHandlerProvider()

  @Bean
  @ConditionalOnMissingBean(SagaRepository::class)
  fun sagaRepository(eventRepository: EventRepository): SagaRepository {
    return DefaultSagaRepository(eventRepository)
  }

  @Bean
  fun sagaService(sagaRepository: SagaRepository, eventRepository: EventRepository, registry: Registry): SagaService {
    return SagaService(sagaRepository, eventRepository, SagaEventHandlerProvider(), registry)
  }

  @Primary
  @Bean
  @ConditionalOnMissingBean(EventPublisher::class)
  fun eventPublisher(sagaService: SagaService): EventPublisher {
    return LocalSagaEventPublisher(sagaService)
  }
}

@ConfigurationProperties("spinnaker.clouddriver.sagas")
class SagaProperties
