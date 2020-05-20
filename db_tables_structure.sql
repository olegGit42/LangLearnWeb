CREATE TABLE public.downloads
(
    count integer NOT NULL DEFAULT 0
)

TABLESPACE pg_default;

ALTER TABLE public.downloads
    OWNER to postgres;

CREATE TABLE public."user"
(
    id integer NOT NULL DEFAULT nextval('user_id_seq'::regclass),
    name character varying(60) COLLATE pg_catalog."default" NOT NULL,
    password_hash character varying(60) COLLATE pg_catalog."default" NOT NULL,
    auth_token character varying(60) COLLATE pg_catalog."default",
    auth_token_buffer character varying(60) COLLATE pg_catalog."default",
    CONSTRAINT user_pkey PRIMARY KEY (id),
    CONSTRAINT ui_name UNIQUE (name)

)

TABLESPACE pg_default;

ALTER TABLE public."user"
    OWNER to postgres;

CREATE TABLE public.user_data
(
    user_id integer NOT NULL,
    max_word_id integer NOT NULL,
    max_tag_id integer NOT NULL,
    CONSTRAINT user_data_pkey PRIMARY KEY (user_id),
    CONSTRAINT fk_user_id FOREIGN KEY (user_id)
        REFERENCES public."user" (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
)

TABLESPACE pg_default;

ALTER TABLE public.user_data
    OWNER to postgres;

CREATE TABLE public.word
(
    user_id integer NOT NULL,
    id integer NOT NULL,
    word character varying(500) COLLATE pg_catalog."default" NOT NULL,
    translate character varying(500) COLLATE pg_catalog."default" NOT NULL,
    date_repeat bigint NOT NULL,
    date_create bigint NOT NULL,
    box smallint NOT NULL,
    repeat_count integer NOT NULL,
    CONSTRAINT ui_word UNIQUE (user_id, id)
,
    CONSTRAINT fk_user_id FOREIGN KEY (user_id)
        REFERENCES public.user_data (user_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
        NOT VALID
)

TABLESPACE pg_default;

ALTER TABLE public.word
    OWNER to postgres;

CREATE TABLE public.tag
(
    user_id integer NOT NULL,
    id integer NOT NULL,
    tag character varying(50) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT ui_tag UNIQUE (user_id, id)
,
    CONSTRAINT fk_user_id FOREIGN KEY (user_id)
        REFERENCES public.user_data (user_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
        NOT VALID
)

TABLESPACE pg_default;

ALTER TABLE public.tag
    OWNER to postgres;

CREATE TABLE public.word_tag
(
    user_id integer NOT NULL,
    tag_id integer NOT NULL,
    word_id integer NOT NULL,
    CONSTRAINT ui_word_tag UNIQUE (user_id, tag_id, word_id)
,
    CONSTRAINT fk_user_id FOREIGN KEY (user_id)
        REFERENCES public.user_data (user_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
        NOT VALID
)

TABLESPACE pg_default;

ALTER TABLE public.word_tag
    OWNER to postgres;