package com.jinnara.reactorpractical

import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.test.test
import reactor.test.StepVerifier

class c02_TransformingSequence {
  @Test
  fun transforming_sequence() {
    val numbersFlux:Flux<Int> = numerical_service()
    numbersFlux.map { it + 1 }.test()
      .expectNext(2, 3, 4, 5, 6, 7, 8, 9, 10, 11)
      .verifyComplete()
  }

  @Test
  fun transforming_sequence_2() {
    val numbersFlux:Flux<Int> = numerical_service_2()
    numbersFlux
      .flatMap { (if (it > 0) { ">" } else if (it < 0) { "<" } else { "=" }).toMono() }
      .test()
      .expectSubscription()
      .expectNext(">", "<", "0", ">", ">")
  }

  @Test
  fun maybe() {
    val result:Mono<String> = maybe_service()
    result
      .switchIfEmpty { "no results".toMono() }
      .test()
      .expectNext("no results")
      .verifyComplete()
  }

  @Test
  fun sequence_sum() {
    val numerics:Flux<Int> = numerical_service()
    numerics
      .reduce { t, u -> t +u }
      .test()
      .expectSubscription()
      .expectNext(55)
      .verifyComplete()
  }

  @Test
  fun sum_each_successive() {
    var sum = 0
    val numerics:Flux<Int> = numerical_service()
    numerics
      .map { sum += it; sum }
      .test()
      .expectSubscription()
      .expectNext(1, 3, 6, 10, 15, 21, 28, 36, 45, 55)
      .verifyComplete()
  }

  @Test
  fun sequence_starts_with_zero() {
    Flux.concat(0.toMono(), numerical_service())
      .test()
      .expectNext(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
      .verifyComplete();
  }

  private fun numerical_service(): Flux<Int> {
    return Flux.range(1, 10)
  }

  private fun object_service(): Flux<Any> {
    return Flux.just("1", "2", "3", "4", "5")
  }

  private fun numerical_service_2(): Flux<Int> {
    return Flux.just(100, -1, 0, 78, 1)
  }

  private fun maybe_service(): Mono<String> {
    return Mono.empty()
  }
}