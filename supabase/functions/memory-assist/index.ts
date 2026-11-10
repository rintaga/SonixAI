import { withSupabase } from 'npm:@supabase/server@^1'

// Supabase Edge Runtime's built-in 384-dimensional gte-small embedding model.
const embeddingModel = new Supabase.ai.Session('gte-small')

const json = (body: unknown, status = 200) =>
  new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json; charset=utf-8' },
  })

export default {
  fetch: withSupabase({ auth: 'user' }, async (req, ctx) => {
    if (req.method !== 'POST') return json({ error: 'Method not allowed' }, 405)

    try {
      const body = await req.json()
      const action = String(body?.action ?? '')

      if (action === 'search') {
        const query = String(body?.query ?? '').trim()
        if (!query) return json({ memories: [] })

        const embedding = await embeddingModel.run(query.slice(0, 4000), {
          mean_pool: true,
          normalize: true,
        })

        const { data, error } = await ctx.supabase.rpc('match_sonix_memories', {
          query_embedding: Array.from(embedding),
          match_threshold: 0.25,
          match_count: Math.max(1, Math.min(Number(body?.limit ?? 6), 10)),
        })
        if (error) return json({ error: error.message }, 500)
        return json({ memories: data ?? [] })
      }

      if (action === 'remember') {
        const incoming = Array.isArray(body?.memories) ? body.memories : []
        const saved: string[] = []

        for (const item of incoming.slice(0, 8)) {
          const content = String(item?.content ?? '').trim().slice(0, 1200)
          const allowed = ['preference', 'project', 'goal', 'fact']
          const category = allowed.includes(String(item?.category)) ? String(item.category) : 'fact'
          if (content.length < 8) continue

          const embedding = await embeddingModel.run(content, {
            mean_pool: true,
            normalize: true,
          })

          const { error } = await ctx.supabase
            .from('sonix_memories')
            .upsert({
              content,
              category,
              embedding: JSON.stringify(Array.from(embedding)),
              updated_at: new Date().toISOString(),
            }, { onConflict: 'user_id,content' })

          if (error) return json({ error: error.message }, 500)
          saved.push(content)
        }

        return json({ ok: true, saved_count: saved.length })
      }

      return json({ error: 'Unknown action. Use search or remember.' }, 400)
    } catch (error) {
      return json({ error: error instanceof Error ? error.message : String(error) }, 500)
    }
  }),
}
