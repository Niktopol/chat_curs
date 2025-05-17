"use client"

import { useEffect } from "react";
import styles from "./modules/chat_user_add_input.module.css";
import { useForm } from "react-hook-form";
import { useRouter } from "next/navigation";
import { API_URL } from "@/lib/config";

export default function ChatUserAddInput({ id }){
    const router = useRouter();
    const {
        register,
        handleSubmit,
        setError,
        clearErrors,
        reset,
        formState: { errors, isSubmitting },
    } = useForm();

    useEffect(() => {
        reset();
    }, [id])

    const onSubmit = async (data) => {
        try {
            const user_add_resp = await fetch(`${API_URL}/chats/users/${id}/add`, {
                method: "PATCH",
                credentials: "include",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({value: data.username}),
            });

            if (!user_add_resp.ok){
                const error = await user_add_resp.json();
                setError("fail", {
                    type: "manual",
                    message: error.error
                });
            } else {
                reset();
            }
        } catch (e) {
            router.push("/login");
        }
    }

    return (
        <form onSubmit={handleSubmit(onSubmit)} className={styles.main}>
            <div className={styles.name}>
                <span className={styles.at}>@</span>
                <input
                    placeholder={"Имя пользователя"}
                    autoComplete="off"
                    autoCorrect="off"
                    className={styles.name_input}
                    {...register("username", {
                        required: "Введите имя пользователя",
                        maxLength: {
                            value: 20,
                            message: "Значение должно быть короче 20 символов"
                        },
                        pattern: {
                            value: /^[A-Za-z0-9\-_]+$/,
                            message: "Допустимы только англ. буквы, цифры, '-' и '_'"
                        },
                        onChange: () => clearErrors(["fail"])
                    })}
                    disabled={isSubmitting}/>
                    <p className={styles.error_msg}>{errors.fail?.message ? errors.fail?.message : (errors.username?.message ? errors.username?.message : "")}</p>
            </div>
            <input type="submit" value={"Добавить пользователя"} disabled={isSubmitting} className={styles.edit_button}/>
        </form>
    );
}