package com.jinnara.reactorpractical

import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.test.test
import java.time.Duration
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicReference

class c01_Introduction {

  @Test
  fun hello_world(){
    "Hello World".toMono().test()
      .expectSubscription()
      .expectNext("Hello World")
      .expectComplete()
      .verify()
  }

  @Test
  fun unresponsive_service() {
    val mono: Mono<String> = Mono.never()
    mono
      .timeout(Duration.ofSeconds(1))
      .test()
      .expectError(TimeoutException::class.java)
      .verify()
  }

  @Test
  fun empty_service() {
    val emptyServiceCalled:AtomicReference<Boolean> = mockk(relaxed = true)

    val mono:Mono<String> = Mono.defer<String?> {
      emptyServiceCalled.set(true)
      Mono.empty()
    }
    mono.test()
      .expectSubscription()
      .expectNextCount(0)
      .verifyComplete()

    verify { emptyServiceCalled.set(true) }
  }

  @Test
  fun multi_result_service() {
    val serviceResult:Flux<String> = Flux.just("valid result")
      .concatWith(Flux.error(RuntimeException("oops, you collected to many, and you broke the service...")))

    serviceResult.test()
      .expectSubscription()
      .expectNext("valid result")
      .expectError(RuntimeException::class.java)
      .verify()
  }

  @Test
  fun fortune_top_five() {
    val coLtd = arrayOf("Walmart", "Amazon", "Apple", "CVS Health", "UnitedHealth Group")
    val fortuneServiceCalled:AtomicReference<Boolean> = mockk(relaxed = true)
    val serviceResult:Flux<String> = Flux.fromArray(coLtd)
    serviceResult
      .doOnNext { fortuneServiceCalled.set(true) }
      .test()
      .expectSubscription()
      .expectNextCount(5)
      .verifyComplete()

    verify { fortuneServiceCalled.set(true) }

  }

  @Test
  fun nothing_happens_until_you() {
    val companyList = CopyOnWriteArrayList<String>()
    val serviceResult = arrayOf("Walmart", "Amazon", "Apple", "CVS Health", "UnitedHealth Group").toFlux()
    serviceResult
      .map {
        companyList.add(it)
        it}.test()
      .expectSubscription()
      .expectNext("Walmart")
      .expectNextCount(4)
      .verifyComplete()

    assertEquals(5, companyList.size)
  }

  @Test
  fun leaving_blocking_world_behind() {
    val fortuneServiceCalled = AtomicReference<Boolean>()
    val companyList = CopyOnWriteArrayList<String>()
    arrayOf("Walmart", "Amazon", "Apple", "CVS Health", "UnitedHealth Group").toFlux()
      .map {
        companyList.add(it)
        it
      }
      .subscribe { fortuneServiceCalled.set(true) }

    assertTrue(fortuneServiceCalled.get())
    assertEquals(5, companyList.size)
  }
}