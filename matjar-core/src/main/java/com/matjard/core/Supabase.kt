package com.matjard.core

/**
 * Supabase connection info for the Matjar D project.
 *
 * PROJECT_URL is the public project gateway. PUBLISHABLE_KEY is the public
 * "publishable" API key: it is designed to be embedded in clients. It can only
 * do what the database's Row Level Security policies allow:
 *   - everybody can read the active catalog + store settings
 *   - anonymous visitors can only place orders (via the place_order RPC, which
 *     computes prices server-side) and track an order by number+phone
 *   - writes require a signed-in user whose id is present in admin_users
 * The service_role/secret keys are NEVER embedded in either app.
 */
object Supabase {
    const val PROJECT_REF = "bvjyrnotlbcdzjywgcai"
    const val PROJECT_URL = "https://bvjyrnotlbcdzjywgcai.supabase.co"
    const val PUBLISHABLE_KEY = "sb_publishable_ZwOdKSoEBvja5zO3K2UHQg_MixUqp37"

    const val PRODUCT_IMAGES_BUCKET = "product-images"
}
