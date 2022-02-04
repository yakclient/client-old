package net.yakclient.client.boot.loader

public class IntegratedCL private constructor(
    parent: ClassLoader,
    private val components: List<ClComponent>
) : ClassLoader(parent) {
    override fun findClass(name: String): Class<*> =
        components.firstNotNullOfOrNull { it.find(name) } ?: throw ClassNotFoundException(name)

    public class ClController internal constructor(
        private val parent: ClassLoader,
        components: MutableList<ClComponent> = ArrayList()
    ) : MutableList<ClComponent> by components {
        public val loader: ClassLoader get() = IntegratedCL(parent, this)
    }
}