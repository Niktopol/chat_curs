import { createAsyncThunk } from "@reduxjs/toolkit";


export const fetchUserSession = createAsyncThunk(
    'userSession/fetch',
    async (_, thunkAPI) => {
        try {
            const session_resp = await fetch("http://localhost:8080/user/profile", {credentials: "include"});
            if (!session_resp.ok) throw new Error("Невозможно получить сессию");

            const session_resp_data = await session_resp.json();
            const profilepic_resp = await fetch(`http://localhost:8080/user/profilepic/${encodeURIComponent(session_resp_data.username)}`, {credentials: "include"});

            if (profilepic_resp.ok) {
                const blob = await profilepic_resp.blob();
                const image = URL.createObjectURL(blob);
                return {...session_resp_data, image}
            }

            return await session_resp_data;
        } catch (error) {
            return thunkAPI.rejectWithValue(error.message);
        }
    }
);