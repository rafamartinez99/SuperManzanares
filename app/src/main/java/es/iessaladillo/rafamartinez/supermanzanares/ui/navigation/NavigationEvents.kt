package es.iessaladillo.rafamartinez.supermanzanares.ui.navigation

import kotlinx.coroutines.flow.MutableSharedFlow

object NavigationEvents {
    val scrollToTop = MutableSharedFlow<String>(extraBufferCapacity = 1)
}
