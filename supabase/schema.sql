-- Sonix AI Advanced Memory schema for Supabase.
-- Run this file in Supabase Dashboard > SQL Editor.

create extension if not exists vector with schema extensions;

create table if not exists public.sonix_conversations (
  id uuid primary key,
  user_id uuid not null default auth.uid() references auth.users(id) on delete cascade,
  title text not null default 'Obrolan Sonix',
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists public.sonix_messages (
  id uuid primary key default gen_random_uuid(),
  conversation_id uuid not null references public.sonix_conversations(id) on delete cascade,
  user_id uuid not null default auth.uid() references auth.users(id) on delete cascade,
  role text not null check (role in ('user', 'assistant', 'system')),
  content text not null,
  created_at timestamptz not null default now()
);

create table if not exists public.sonix_memories (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null default auth.uid() references auth.users(id) on delete cascade,
  content text not null,
  category text not null default 'fact',
  embedding extensions.vector(384),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  constraint sonix_memories_category_check check (category in ('preference', 'project', 'goal', 'fact')),
  constraint sonix_memories_user_content_unique unique (user_id, content)
);

create index if not exists sonix_conversations_user_updated_idx
  on public.sonix_conversations (user_id, updated_at desc);
create index if not exists sonix_messages_conversation_created_idx
  on public.sonix_messages (conversation_id, created_at asc);
create index if not exists sonix_memories_user_updated_idx
  on public.sonix_memories (user_id, updated_at desc);
create index if not exists sonix_memories_embedding_hnsw_idx
  on public.sonix_memories using hnsw (embedding vector_cosine_ops);

alter table public.sonix_conversations enable row level security;
alter table public.sonix_messages enable row level security;
alter table public.sonix_memories enable row level security;

-- A signed-in user can only access their own conversations, messages, and memories.
drop policy if exists "sonix_conversations_select_own" on public.sonix_conversations;
create policy "sonix_conversations_select_own" on public.sonix_conversations
  for select to authenticated using (auth.uid() = user_id);
drop policy if exists "sonix_conversations_insert_own" on public.sonix_conversations;
create policy "sonix_conversations_insert_own" on public.sonix_conversations
  for insert to authenticated with check (auth.uid() = user_id);
drop policy if exists "sonix_conversations_update_own" on public.sonix_conversations;
create policy "sonix_conversations_update_own" on public.sonix_conversations
  for update to authenticated using (auth.uid() = user_id) with check (auth.uid() = user_id);
drop policy if exists "sonix_conversations_delete_own" on public.sonix_conversations;
create policy "sonix_conversations_delete_own" on public.sonix_conversations
  for delete to authenticated using (auth.uid() = user_id);

drop policy if exists "sonix_messages_select_own" on public.sonix_messages;
create policy "sonix_messages_select_own" on public.sonix_messages
  for select to authenticated using (auth.uid() = user_id);
drop policy if exists "sonix_messages_insert_own" on public.sonix_messages;
create policy "sonix_messages_insert_own" on public.sonix_messages
  for insert to authenticated with check (auth.uid() = user_id);
drop policy if exists "sonix_messages_delete_own" on public.sonix_messages;
create policy "sonix_messages_delete_own" on public.sonix_messages
  for delete to authenticated using (auth.uid() = user_id);

drop policy if exists "sonix_memories_select_own" on public.sonix_memories;
create policy "sonix_memories_select_own" on public.sonix_memories
  for select to authenticated using (auth.uid() = user_id);
drop policy if exists "sonix_memories_insert_own" on public.sonix_memories;
create policy "sonix_memories_insert_own" on public.sonix_memories
  for insert to authenticated with check (auth.uid() = user_id);
drop policy if exists "sonix_memories_update_own" on public.sonix_memories;
create policy "sonix_memories_update_own" on public.sonix_memories
  for update to authenticated using (auth.uid() = user_id) with check (auth.uid() = user_id);
drop policy if exists "sonix_memories_delete_own" on public.sonix_memories;
create policy "sonix_memories_delete_own" on public.sonix_memories
  for delete to authenticated using (auth.uid() = user_id);

create or replace function public.match_sonix_memories(
  query_embedding extensions.vector(384),
  match_threshold float default 0.25,
  match_count int default 6
)
returns table (
  id uuid,
  content text,
  category text,
  similarity float
)
language sql
stable
security invoker
set search_path = public, extensions
as $$
  select
    m.id,
    m.content,
    m.category,
    (1 - (m.embedding <=> query_embedding))::float as similarity
  from public.sonix_memories as m
  where m.user_id = auth.uid()
    and m.embedding is not null
    and (1 - (m.embedding <=> query_embedding)) >= match_threshold
  order by m.embedding <=> query_embedding
  limit greatest(1, least(match_count, 20));
$$;

grant select, insert, update, delete on public.sonix_conversations to authenticated;
grant select, insert, delete on public.sonix_messages to authenticated;
grant select, insert, update, delete on public.sonix_memories to authenticated;
grant execute on function public.match_sonix_memories(extensions.vector, float, int) to authenticated;
