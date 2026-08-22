import { createSlice } from '@reduxjs/toolkit';

const saved = JSON.parse(localStorage.getItem('crop-auth') || 'null');

const authSlice = createSlice({
  name: 'auth',
  initialState: saved || { token: null, user: null },
  reducers: {
    setCredentials(state, action) {
      state.token = action.payload.token;
      state.user = action.payload;
      localStorage.setItem('crop-auth', JSON.stringify(state));
    },
    logout(state) {
      state.token = null;
      state.user = null;
      localStorage.removeItem('crop-auth');
    },
  },
});

export const { setCredentials, logout } = authSlice.actions;
export default authSlice.reducer;
