"use client"

import { useDispatch, useSelector } from "react-redux";
import styles from "./modules/message_input.module.css"
import { useForm } from "react-hook-form";
import { useRouter } from "next/navigation";
import { setChat } from "@/store/chatSelectedSlice";
import { useEffect, useRef, useState } from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faPaperclip, faPaperPlane, faXmark } from "@fortawesome/free-solid-svg-icons";
import { API_URL } from "@/lib/config";

function InputForm() {
    const {
        register,
        watch,
        handleSubmit,
        setValue,
        reset,
        formState: { isSubmitting },
    } = useForm();
    const chat = useSelector((state) => state.chatSelected);
    const [attachment, setAttachment] = useState(null);
    const [imageErrorMsg, setImageErrorMsg] = useState("");
    const message = watch("message");
    const textareaRef = useRef(null);
    const router = useRouter();

    useEffect(() => {
        reset();
        setAttachment(null);
    }, [chat.id])

    useEffect(() => {
        const textarea = textareaRef.current;
        if (textarea) {
            textarea.style.height = "auto";
            textarea.style.height = `${Math.min(textarea.scrollHeight, 5 * 24)}px`;
        }
    }, [message]);

    const onSubmit = async (data) => {
        setImageErrorMsg("");
        try {
            if (data.file && data.file.length) {
                const formData = new FormData();
                formData.append('file', data.file[0]);
                const res = await fetch(`${API_URL}/messages/image/${chat.id}`, {
                    method: "POST",
                    credentials: "include",
                    body: formData
                });

                if (!res.ok) {
                    setAttachment(null);
                    setValue("file", null);
                    setImageErrorMsg((await res.json())?.error);
                    return;
                }

            }

            if (data.message) {
                await fetch(`${API_URL}/messages/${chat.id}`, {
                    method: "POST",
                    credentials: "include",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ value: data.message }),
                });
            }

            reset();
            setAttachment(null);
        } catch (e) {
            router.push("/login");
        }
    };

    return (
        <form onSubmit={handleSubmit(onSubmit)} className={styles.input_wrapper}>
            <div className={styles.text_wrapper}>
                <label htmlFor="image" className={styles.attachment}>
                    <FontAwesomeIcon icon={faPaperclip} className={styles.attachment_icon}/>
                    {
                        attachment ? 
                        <>
                            <div className={styles.attachment_name}>{attachment}</div>
                            <div className={styles.attachment_cross}>
                                <FontAwesomeIcon icon={faXmark} className={styles.attachment_cross_icon}/>
                            </div>
                        </> :
                        <></>
                    }
                    
                </label>
                <input type="file"
                    accept="image/jpeg"
                    id="image"
                    style={{display: "none"}}
                    {...register("file", {
                        onChange: (e) => {
                            if (e.target.files[0].size < 5 * 1024 * 1024) {
                                setAttachment(e.target.files[0].name);
                                setImageErrorMsg("");
                            } else {
                                setAttachment(null);
                                setValue("file", null);
                                setImageErrorMsg("Размер изображения > 5 МБ");
                            }
                        }
                    })}

                    onClick={(e) => {
                        if (attachment) {
                            e.preventDefault();
                            setAttachment(null);
                            setValue("file", null);
                            setImageErrorMsg("");
                        }
                    }}
                    disabled={isSubmitting}/>
                <textarea
                {...register("message", {
                    onChange : () => {
                        setImageErrorMsg("");
                    }
                })}
                onKeyDown={(e) => {
                    if (e.key === "Enter" && !e.shiftKey) {
                        e.preventDefault();
                        handleSubmit(onSubmit)();
                    }
                }}
                ref={(e) => {
                    register("message").ref(e);
                    textareaRef.current = e;
                }}
                placeholder="Введите сообщение..."
                autoComplete="off"
                autoCorrect="off"
                maxLength={2000}
                rows={1}
                disabled={isSubmitting}
                className={styles.text}/>
                <p className={styles.file_error_msg}>{imageErrorMsg}</p>
            </div>

            <button type="submit" disabled={isSubmitting} className={styles.send_button}>
                <FontAwesomeIcon icon={faPaperPlane} className={styles.send_icon}/>
            </button>
        </form>
    );
}

export default function MessageInput(){
    const chat = useSelector((state) => state.chatSelected);
    const dispatch = useDispatch();
    const {
        handleSubmit,
        formState: { isSubmitting },
    } = useForm();
    const router = useRouter();

    const onSubmit = async () => {
        try {
            const res = await fetch(`${API_URL}/chats/create/${encodeURIComponent(chat.username)}`, {
                method: "POST",
                credentials: "include",
                headers: { "Content-Type": "application/json" }
            });

            if (!res.ok) {
                throw new Error((await res.json())?.error);
            } else {
                dispatch(setChat(await res.json()));
            }
        } catch (e) {
            router.push("/login");
        }
    };

    return (
        <div className={styles.main}>
            { chat.new ?
                <form onSubmit={handleSubmit(onSubmit)}>
                    <input type="submit" value={"Начать общение"} disabled={isSubmitting} className={styles.chat_create}/>
                </form> :
                <InputForm></InputForm>
            }
        </div>
    );
}