package pl.stosik.billing.core.extensions.logger

import mu.KLogger
import mu.KotlinLogging

fun <R : Any> R.logger(): Lazy<KLogger> {
    return lazy { KotlinLogging.logger(unwrapCompanionClass(this.javaClass).name) }
}

private fun <T : Any> unwrapCompanionClass(ofClass: Class<T>): Class<*> {
    return if (ofClass.enclosingClass != null && ofClass.enclosingClass.kotlin.objectInstance?.javaClass == ofClass) {
        ofClass.enclosingClass
    } else {
        ofClass
    }
}