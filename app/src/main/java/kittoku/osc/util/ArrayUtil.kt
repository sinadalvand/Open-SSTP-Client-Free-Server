package kittoku.osc.util


fun <E> List<E>.toArrayList(): ArrayList<E> {
    return arrayListOf<E>().also { it.addAll(this) }
}